/*
 *
 *  * This file is part of the Hesperides distribution.
 *  * (https://github.com/voyages-sncf-technologies/hesperides)
 *  * Copyright (c) 2016 VSCT.
 *  *
 *  * Hesperides is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as
 *  * published by the Free Software Foundation, version 3.
 *  *
 *  * Hesperides is distributed in the hope that it will be useful, but
 *  * WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  * General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package com.vsct.dt.hesperides.security;

import com.vsct.dt.hesperides.security.model.User;
import com.vsct.dt.hesperides.storage.UserInfo;
import com.vsct.dt.hesperides.storage.UserProvider;

/**
 * Created by william_montaz on 25/02/2015.
 */
public class ThreadLocalUserContext implements UserProvider, UserContext {

    /**
     * ThreadLocal holding user
     */
    private ThreadLocal<User>            userThreadLocal = new ThreadLocal<>();

    public User getCurrentUser() {
        User user = userThreadLocal.get();

        if (user == null) {
            return User.UNTRACKED;
        } else {
            return user;
        }
    }

    @Override
    public UserInfo getCurrentUserInfo() {
        return new UserInfo(getCurrentUser().getUsername());
    }

    public void setCurrentUser(User user) {
        userThreadLocal.set(user);
    }
}
