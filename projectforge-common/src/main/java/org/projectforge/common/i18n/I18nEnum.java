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

package org.projectforge.common.i18n;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public interface I18nEnum {
  String getI18nKey();

  /**
   * Used for accessing i18n keys through reflection.
   * @param clazz The class name of the I18nEnum (e. g. org.projectforge.business.books.BookStatus)
   * @param value The value to set (e. g. 'MISSED').
   */
  @SuppressWarnings("unchecked")
  static Enum<? extends I18nEnum> create(Class clazz, String value) {
    return Enum.valueOf((Class<Enum>) clazz, value);
  }
}
