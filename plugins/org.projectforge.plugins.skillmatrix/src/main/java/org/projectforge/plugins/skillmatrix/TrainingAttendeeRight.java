/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2020 Micromata GmbH, Germany (www.micromata.com)
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

package org.projectforge.plugins.skillmatrix;

import org.projectforge.business.multitenancy.TenantRegistryMap;
import org.projectforge.business.user.UserGroupCache;
import org.projectforge.business.user.UserRightAccessCheck;
import org.projectforge.business.user.UserRightCategory;
import org.projectforge.business.user.UserRightValue;
import org.projectforge.common.StringHelper;
import org.projectforge.framework.access.AccessChecker;
import org.projectforge.framework.access.OperationType;
import org.projectforge.framework.persistence.user.entities.PFUserDO;

/**
 * Define the access rights.
 * 
 * @author Werner Feder (werner.feder@t-online.de)
 */
public class TrainingAttendeeRight extends UserRightAccessCheck<TrainingAttendeeDO>
{
  private static final long serialVersionUID = -3590945654632199595L;

  private transient UserGroupCache userGroupCache;

  /**
   * @param id
   * @param category
   * @param rightValues
   */
  public TrainingAttendeeRight(AccessChecker accessChecker)
  {
    super(accessChecker, SkillmatrixPluginUserRightId.PLUGIN_SKILL_MATRIX_TRAINING_ATTENDEE,
        UserRightCategory.PLUGINS,
        UserRightValue.TRUE);
  }

  private UserGroupCache getUserGroupCache()
  {
    if (userGroupCache == null) {
      userGroupCache = TenantRegistryMap.getInstance().getTenantRegistry().getUserGroupCache();
    }
    return userGroupCache;
  }

  @Override
  public boolean hasAccess(final PFUserDO user, final TrainingAttendeeDO obj, final TrainingAttendeeDO oldObj,
      final OperationType operationType)
  {
    if (accessChecker.isUserMemberOfAdminGroup(user)) {
      return true;
    }

    final TrainingAttendeeDO attendee = (oldObj != null) ? oldObj : obj;
    if (attendee == null) {
      return true;
    }
    if (operationType == OperationType.SELECT) {
      return (hasAccess(StringHelper.splitToIntegers(attendee.getTraining().getReadOnlyAccessGroupIds(), ","),
          user.getId()))
          || (hasAccess(StringHelper.splitToIntegers(attendee.getTraining().getFullAccessGroupIds(), ","),
          user.getId()))
          || (user.getId() == attendee.getAttendeeId());
    }
    return hasAccess(StringHelper.splitToIntegers(attendee.getTraining().getFullAccessGroupIds(), ","), user.getId());
  }

  private boolean hasAccess(final Integer[] groupIds, final Integer userId)
  {
    if (getUserGroupCache().isUserMemberOfAtLeastOneGroup(userId, groupIds)) {
      return true;
    }
    return false;
  }
}
