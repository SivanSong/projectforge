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

package org.projectforge.business.teamcal.event.ical.converter;

import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.RRule;
import org.projectforge.business.teamcal.event.model.TeamEventDO;

public class RRuleConverter extends PropertyConverter
{
  @Override
  public Property toVEvent(final TeamEventDO event)
  {
    if (event.hasRecurrence()) {
      return event.getRecurrenceRuleObject();
    }

    return null;
  }

  @Override
  public boolean fromVEvent(final TeamEventDO event, final VEvent vEvent)
  {
    final RRule rule = (RRule) vEvent.getProperty(Property.RRULE);
    event.setRecurrence(rule);

    return true;
  }
}
