/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2019 Micromata GmbH, Germany (www.micromata.com)
//
// ProjectForge is dual-licensed.
//
// This community edition is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License as published
// by the Free Software Foundation; version 3 of the License.
//
// This community edition is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
// Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, see http://www.gnu.org/licenses/.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.business.common

import javax.persistence.Column
import javax.persistence.MappedSuperclass

import org.projectforge.framework.persistence.entities.DefaultBaseDO

/**
 * Created by blumenstein on 18.07.17.
 */
@MappedSuperclass
open class BaseUserGroupRightsDO : DefaultBaseDO() {

    /**
     * Members of these groups have full read/write access to all entries of this object.
     */
    @get:Column(name = "full_access_group_ids", nullable = true)
    var fullAccessGroupIds: String? = null

    @get:Column(name = "full_access_user_ids", nullable = true)
    var fullAccessUserIds: String? = null

    /**
     * Members of these groups have full read-only access to all entries of this object.
     */
    @get:Column(name = "readonly_access_group_ids", nullable = true)
    var readonlyAccessGroupIds: String? = null

    /**
     * These users have full read-only access to all entries of this object.
     */
    @get:Column(name = "readonly_access_user_ids", nullable = true)
    var readonlyAccessUserIds: String? = null

    /**
     * Members of these group have read-only access to all entries of this object, but they can only see the event start
     * and stop time
     */
    @get:Column(name = "minimal_access_group_ids", nullable = true)
    var minimalAccessGroupIds: String? = null

    /**
     * Members of this group have only access to the start and stop time, nothing else.
     */
    @get:Column(name = "minimal_access_user_ids", nullable = true)
    var minimalAccessUserIds: String? = null
}
