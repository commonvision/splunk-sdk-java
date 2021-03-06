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

import java.util.Map;

/**
 * Representation of a collection of user entities.
 */
public class UserCollection extends EntityCollection<User> {

    /**
     * Constructs an instance of {@code UserCollection}.
     *
     * @param service This service instance this collection is affiliated with.
     */
    UserCollection(Service service) {
        super(service, "authentication/users", User.class);
    }

    /**
     * Create a new user entity using the given name, password and role.
     *
     * @param name The name for the new user entity.
     * @param password The password for the new user entity.
     * @param role The role to assign to the new entity.
     * @return The newly craeted user entity.
     */
    public User create(String name, String password, String role) {
        return create(name, password, role, null);
    }

    /**
     * Create a new user entity using the given name, password and array of
     * roles.
     *
     * @param name The name for the new user entity.
     * @param password The password for the new user entity.
     * @param roles Array of roles to assign to the new user entity.
     * @return The newly created user entity.
     */
    public User create(String name, String password, String[] roles) {
        return create(name, password, roles, null);
    }

    /**
     * Create a new user entity using the given name, password, role and
     * map of extra args.
     *
     * @param name The name for the new user entity.
     * @param password The password for the new user entity.
     * @param role The role to assign to the new entity.
     * @param args A map of extra arguments for the new user entity.
     * @return The newly created user entity.
     */
    public User create(String name, String password, String role, Map args) {
        args = Args.create(args);
        args.put("password", password);
        args.put("roles", role);
        return create(name, args);
    }


    /**
     * Create a new user entity using the given name, password and array of
     * roles.
     *
     * @param name The name for the new user entity.
     * @param password The password for the new user entity.
     * @param roles Array of roles to assign to the new user entity.
     * @param args A map of extra arguments for the new user entity.
     * @return The newly created user entity.
     */
    public User 
    create(String name, String password, String[] roles, Map args) {
        args = Args.create(args);
        args.put("password", password);
        args.put("roles", roles);
        return create(name, args);
    }
}
