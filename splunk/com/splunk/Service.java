/*
 * Copyright 2011 Splunk, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"): you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.splunk;

import java.io.InputStream;
import java.util.Map;

/**
 * Representation of a Splunk service at a given address (host:port) accessed
 * using a given protocol "scheme" ({@code http} or {@code https}). A service
 * instance also captures an optional namespace context consisting of
 * an optional owner name (or "-" wildcard) and optional app name (or "-"
 * wildcard. In order to access {@code Service} members the instance must
 * be authenticated by presenting credentials using the {@code login} method
 * or by constructing the instance using the {@code connect} method which
 * both creates and authenticates the instance.
 */
public class Service extends HttpService {
    /** The current app context */
    protected String app = null;

    /** The current session token */
    protected String token = null;

    /** The current owner context */
    protected String owner = null;

    /** The username used to authenticate the instance */
    protected String username = null;

    /** The password used to authenticate the instance */
    protected String password = null;

    /** The default host name, used if no host is provided */
    public static String DEFAULT_HOST = "localhost";

    /** The default port number, used if no port is provided */
    public static int DEFAULT_PORT = 8089;

    /** The default scheme, used if no scheme is provided */
    public static String DEFAULT_SCHEME = "https";

    /**
     * Creates a new {@code Service} instance using the given host.
     *
     * @param host Host name of the service.
     */
    public Service(String host) {
        super(host);
    }

    /**
     * Creates a new {@code Service} instance using the given host and port.
     *
     * @param host Host name of the service.
     * @param port Port number of the service
     */
    public Service(String host, int port) {
        super(host, port);
    }

    /**
     * Creates a new {@code Service} instance using the given host, port and
     * scheme.
     *
     * @param host Host name of the service.
     * @param port Port number of the service.
     * @param scheme Scheme for accessing the service
     *        ({@code http} or {@code https}).
     */
    public Service(String host, int port, String scheme) {
        super(host, port, scheme);
    }

    /**
     * Creates a new {@code Service} instance using the given
     * {@code ServiceArgs}
     *
     * @param args {@code ServiceArgs} to initialize the service.
     */
    public Service(ServiceArgs args) {
        super();
        this.app = args.app;
        this.host = args.host == null ? DEFAULT_HOST : args.host;
        this.owner = args.owner;
        this.port = args.port == null ? DEFAULT_PORT : args.port;
        this.scheme = args.scheme == null ? DEFAULT_SCHEME : args.scheme;
        this.token = args.token;
    }

    /**
     * Creates a new {@code Service} instance using the given {@code Map} of
     * service arguments.
     *
     * @param args {@code Map} of arguments to initialize the service.
     */
    public Service(Map<String, Object> args) {
        super();
        this.app = Args.<String>get(args, "app", null);
        this.host = Args.<String>get(args, "host", DEFAULT_HOST);
        this.owner = Args.<String>get(args, "owner", null);
        this.port = Args.<Integer>get(args, "port", DEFAULT_PORT);
        this.scheme = Args.<String>get(args, "scheme", DEFAULT_SCHEME);
        this.token = Args.<String>get(args, "token", null);
    }

    /**
     * Establish a connection to a Splunk service using the given arguments.
     * This member constructs a new {@code Service} instance and authenticates
     * the session using credentials passed in the {@code args} map.
     *
     * @param args Map of arguments used to initialize and authenticate the
     *             sevice.
     * @return A new {@code Service} instance.
     */
    public static Service connect(Map<String, Object> args) {
        Service service = new Service(args);
        if (args.containsKey("username")) {
            String username = Args.get(args, "username", null);
            String password = Args.get(args, "password", null);
            service.login(username, password);
        }
        return service;
    }

    /**
     * Execute a search using the export endpoint which streams results back
     * in the returned {@code InputStream}.
     *
     * @param search The search query to execute.
     * @return {@code InputStream} containing the search results.
     */
    public InputStream export(String search) {
        return export(search, null);
    }

    /**
     * Execute a search using the export endpoint which streams results back
     * in the returned {@code InputStream}.
     *
     * @param search The search query to execute.
     * @param args Additional search arguments.
     * @return {@code InputStream} containing the search results.
     */
    public InputStream export(String search, Map args) {
        args = Args.create(args).add("search", search);
        ResponseMessage response = get("search/jobs/export", args);
        return response.getContent();
    }

    /**
     * Ensures that the given path is fully qualified, prepending a path
     * prefix if necessarry. The path prefix will be constructed using the
     * current owner & app context if available.
     *
     * @param path The path to verify.
     * @return A fully qualified resource path.
     */
    String fullpath(String path) {
        if (path.startsWith("/"))
            return path;
        if (owner == null && app == null)
            return "/services/" + path;
        return String.format("/servicesNS/%s/%s/%s", 
            owner == null ? "-" : owner, app == null ? "-" : app, path);
    }

    /**
     * Returns the app context for this service instance, {@code null}
     * indicates no app context and {@code "-"} indicates an app wildcard.
     *
     * @return The app context for the service.
     */
    public String getApp() {
        return this.app;
    }

    /**
     * Returns the collection of applications.
     *
     * @return Application collection.
     */
    public EntityCollection<Application> getApplications() {
        return new EntityCollection<Application>(
            this, "apps/local", Application.class);
    }

    /**
     * Returns the collection of configurations.
     *
     * @return Configurations collection.
     */
    public ConfCollection getConfs() {
        return new ConfCollection(this);
    }

    /**
     * Returns an array of system capabilities.
     *
     * @return Capabilities.
     */
    public String[] getCapabilities() {
        Entity caps = new Entity(this, "authorization/capabilities");
        return caps.getStringArray("capabilities");
    }

    /**
     * Returns deployment client configuration and status.
     *
     * @return Deployment client configuration and status.
     */
    public DeploymentClient getDeploymentClient() {
        return new DeploymentClient(this);
    }

    /**
     * Returns the configuration of all deployment servers.
     *
     * @return Configuration of deployment servers.
     */
    public EntityCollection<DeploymentServer> getDeploymentServers() {
        return new EntityCollection<DeploymentServer>(
            this, "deployment/server", DeploymentServer.class);
    }

    /**
     * Returns collection of deployment server class configurations.
     *
     * @return Collection of server class configurations.
     */
    public EntityCollection<DeploymentServerClass> getDeploymentServerClasses(){
        return new EntityCollection<DeploymentServerClass>(
            this, "deployment/serverclass", DeploymentServerClass.class);
    }

    /**
     * Returns collection of multi-tenant configurations.
     *
     * @return Colleciton of multi-tenant configurations.
     */
    public EntityCollection<DeploymentTenant> getDeploymentTenants() {
        return new EntityCollection<DeploymentTenant>(
            this, "deployment/tenants", DeploymentTenant.class);
    }

    /**
     * Returns information regarding distributed search options.
     *
     * @return Distributed search information.
     */
    public DistributedConfiguration getDistributedConfiguration() {
        return new DistributedConfiguration(this);
    }


    /**
     * Returns collection of distributed search peers. A search peer is a
     * Splunk server to which another Splunk server distributes searches. The
     * Splunk server where the search originates is referred to as the search
     * head.
     *
     * @return Collection of search peers.
     */
    public EntityCollection<DistributedPeer> getDistributedPeers() {
        return new EntityCollection<DistributedPeer>(
            this, "search/distributed/peers", DistributedPeer.class);
    }

    /**
     * Returns collection of saved event types.
     *
     * @return Collection of saved event types.
     */
    public EventTypeCollection getEventTypes() {
        return new EventTypeCollection(this);
    }

    /**
     * Returns collection of alerts that have been fired by the service.
     *
     * @return Collection of fired alerts.
     */
    public EntityCollection<FiredAlert> getFiredAlerts() {
        return new EntityCollection<FiredAlert>(
            this, "alerts/fired_alerts", FiredAlert.class);
    }

    /**
     * Returns collection of Splunk indexes.
     *
     * @return Collection of indexes.
     */
    public EntityCollection<Index> getIndexes() {
        return new EntityCollection<Index>(this, "data/indexes", Index.class);
    }

    /**
     * Returns information about the Splunk service.
     *
     * @return Splunk service information.
     */
    public ServiceInfo getInfo() {
        return new ServiceInfo(this);
    }

    /**
     * Returns a collection of configured inputs.
     *
     * @return Collection of inputs.
     */
    public InputCollection getInputs() {
        return new InputCollection(this);
    }

    /**
     * Returns a collection of current search jobs.
     *
     * @return Collection of search jobs.
     */
    public JobCollection getJobs() {
        return new JobCollection(this);
    }

    /**
     * Returns collection of license group configurations.
     *
     * @return Collectio nof license group configurations.
     */
    public EntityCollection<LicenseGroup> getLicenseGroups() {
        return new EntityCollection<LicenseGroup>(
            this, "licenser/groups", LicenseGroup.class);
    }

    /**
     * Returns collection of messages from the licenser.
     *
     * @return Collection of licenser messages.
     */
    public EntityCollection<LicenseMessage> getLicenseMessages() {
        return new EntityCollection<LicenseMessage>(
            this, "licenser/messages", LicenseMessage.class);
    }

    /**
     * Returns the current owner context for this service instance,
     * {@code "-"} indicates wildcard and {@code null} indicates no owner
     * context.
     *
     * @return Current owner context.
     */
    public String getOwner() {
        return this.owner;
    }

    /**
     * Returns collection of licenser pool configurations.
     *
     * @return Collection of licenser pool configurations.
     */
    public LicensePoolCollection getLicensePools() {
        return new LicensePoolCollection(this);
    }

    /**
     * Returns collection of slaves reporting to this license master.
     *
     * @return Collection of licenser slaves.
     */
    public EntityCollection<LicenseSlave> getLicenseSlaves() {
        return new EntityCollection<LicenseSlave>(
            this, "licenser/slaves", LicenseSlave.class);
    }

    /**
     * Returns collection of license stack configurations.
     *
     * @return Collection of license stack configurations.
     */
    public EntityCollection<LicenseStack> getLicenseStacks() {
        return new EntityCollection<LicenseStack>(
            this, "licenser/stacks", LicenseStack.class);
    }

    /**
     * Returns collection of licenses for this service.
     *
     * @return Collection of licenses.
     */
    public EntityCollection<License> getLicenses() {
        return new EntityCollection<License>(
            this, "licenser/licenses", License.class);
    }

    /**
     * Returns collection of service logging categories and their status.
     *
     * @return Collection of logging categories.
     */
    public EntityCollection<Logger> getLoggers() {
        return new EntityCollection<Logger>(
            this, "server/logger", Logger.class);
    }

    /**
     * Returns collection of system messages.
     *
     * @return Collection of system messages.
     */
    public MessageCollection getMessages() {
        return new MessageCollection(this);
    }

    /**
     * Returns global TCP output properties.
     *
     * @return Global TCP output properties.
     */
    public OutputDefault getOutputDefault() {
        return new OutputDefault(this);
    }

    /**
     * Returns collection of output group configurations.
     *
     * @return Collection of output group configurations.
     */
    public EntityCollection<OutputGroup> getOutputGroups() {
        return new EntityCollection<OutputGroup>(
            this, "data/outputs/tcp/group", OutputGroup.class);
    }

    /**
     * Returns collection of data forwarding configurations.
     *
     * @return Collection of data forwarding configurations.
     */
    public EntityCollection<OutputServer> getOutputServers() {
        return new EntityCollection<OutputServer>(
            this, "data/outputs/tcp/server", OutputServer.class);
    }

    /**
     * Returns collection of configurations for forwarding data in standard
     * syslog format.
     *
     * @return Collection of syslog forwarders.
     */
    public EntityCollection<OutputSyslog> getOutputSyslogs() {
        return new EntityCollection<OutputSyslog>(
            this, "data/outputs/tcp/syslog", OutputSyslog.class);
    }

    /**
     * Returns the current password use to authenticate the session.
     *
     * @return Current password.
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Return collection of passwords, this collection is used for the
     * management of secure credentials.
     *
     * @return Collection of passwords.
     */
    public PasswordCollection getPasswords() {
        return new PasswordCollection(this);
    }

    /**
     * Returns a collection of Splunk user roles.
     *
     * @return Collection of user roles.
     */
    public EntityCollection<Role> getRoles() {
        return new EntityCollection<Role>(
            this, "authentication/roles", Role.class);
    }

    /**
     * Returns a collection of saved searches.
     *
     * @return Collection of saved searches.
     */
    public SavedSearchCollection getSavedSearches() {
        return new SavedSearchCollection(this);
    }

    /**
     * Returns service configuration information for an instance of Splunk.
     *
     * @return Service configuration information.
     */
    public Settings getSettings() {
        return new Settings(this);
    }

    /**
     * Returns the current session token. This allows sharing of session tokens
     * across multiple service instances.
     *
     * @return Session token.
     */
    public String getToken() {
        return this.token;
    }

    /**
     * Returns the username used to authenticate the current session.
     *
     * @return Current username.
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Returns collection of Splunk users.
     *
     * @return Collection of users.
     */
    public UserCollection getUsers() {
        return new UserCollection(this);
    }

    /**
     * Authenticate the instance using the given credentials.
     *
     * @param username User name.
     * @param password Password.
     * @return The current service instance.
     */
    public Service login(String username, String password) {
        this.username = username;
        this.password = password;

        Args args = new Args();
        args.put("username", username);
        args.put("password", password);
        ResponseMessage response = post("/services/auth/login", args);
        String sessionKey = Xml.parse(response.getContent())
            .getElementsByTagName("sessionKey")
            .item(0)
            .getTextContent();
        this.token = "Splunk " + sessionKey;

        return this;
    }

    /**
     * Forget the current session token.
     *
     * @return The current service instance.
     */
    public Service logout() {
        this.token = null;
        return this;
    }

    /**
     * Creates a 'oneshot' synchronous search.
     *
     * @param query The search query string.
     * @return The response message of the search.
     */
    public InputStream oneshot(String query) {
       return oneshot(query, null);
    }

    /**
     * Creates a 'oneshot' synchronous search.
     *
     * @param query The search query.
     * @param args The arguments to the search.
     * @return The results of the search.
     */
    public InputStream oneshot(String query, Map args) {
        args = Args.create(args);
        args.put("search", query);
        args.put("exec_mode", "oneshot");
        ResponseMessage response = post("search/jobs/", args);
        return response.getContent();
    }

    /**
     * Parse the given search query and return a semantic map for the search.
     *
     * @param query The query to parse.
     * @return Parse response message.
     */
    public ResponseMessage parse(String query) {
        return parse(query, null);
    }

    /**
     * Parse the given search query and return a semantic map for the search.
     *
     * @param query The query to parse.
     * @param args Additional parse arguments.
     * @return Parse response message.
     */
    public ResponseMessage parse(String query, Map args) {
        args = Args.create(args).add("q", query);
        return get("search/parser", args);
    }

    /**
     * Restart the service. The service will be unavailable until it has
     * sucessfully restarted.
     *
     * @return Restart response message.
     */
    public ResponseMessage restart() {
        return get("server/control/restart");
    }

    /**
     * Issue an HTTP request against the service using the given path and
     * request message. This method overrides the base HttpService send method
     * and applies the Splunk auth header which is required for authenticated
     * interactions with the Splunk service.
     *
     * @param path Request path.
     * @param request Request message.
     * @return HTTP response.
     */
    @Override public ResponseMessage send(String path, RequestMessage request) {
        request.getHeader().put("Authorization", token);
        return super.send(fullpath(path), request);
    }

    /**
     * Provides a session token for use by this service instance. This allows
     * sharing of session tokens across multiple service instances.
     *
     * @param value Session token.
     */
    public void setToken(String value) {
        this.token = value;
    }
}
