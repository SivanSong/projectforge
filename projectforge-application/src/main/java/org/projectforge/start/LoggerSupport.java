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

package org.projectforge.start;

import org.apache.commons.lang3.StringUtils;

/**
 * Helper for logging very important information and warnings.
 */
class LoggerSupport {
  private org.slf4j.Logger log;
  private Alignment alignment;
  private int number;

  LoggerSupport(org.slf4j.Logger log) {
    this(log, Priority.IMPORTANT);
  }

  LoggerSupport(org.slf4j.Logger log, Priority priority) {
    this(log, priority, Alignment.CENTER);
  }

  LoggerSupport(org.slf4j.Logger log, Alignment alignment) {
    this(log, Priority.IMPORTANT, alignment);
  }

  LoggerSupport(org.slf4j.Logger log, Priority priority, Alignment alignment) {
    this.log = log;
    this.alignment = alignment;
    switch (priority) {
      case HIGH:
        this.number = 1;
        break;
      case VERY_IMPORTANT:
        this.number = 5;
        break;
      default:
        this.number = 2;
    }
  }


  enum Priority {HIGH, IMPORTANT, VERY_IMPORTANT}

  enum Alignment {CENTER, LEFT}

  private static final int CONSOLE_LENGTH = 80;

  void logStartSeparator() {
    for (int i = 0; i < number; i++) {
      log.info(StringUtils.rightPad("", CONSOLE_LENGTH, "*") + asterisks(number * 2 + 2));
    }
    log("");
  }

  void logEndSeparator() {
    log("");
    for (int i = 0; i < number; i++) {
      log.info(StringUtils.rightPad("", CONSOLE_LENGTH, "*") + asterisks(number * 2 + 2));
    }
  }

  void log(String text) {
    String padText = alignment == Alignment.LEFT ? StringUtils.rightPad(text, CONSOLE_LENGTH)
            : StringUtils.center(text, CONSOLE_LENGTH);
    log.info(asterisks(number) + " " + padText + " " + asterisks(number));
  }

  private static String asterisks(int number) {
    return StringUtils.rightPad("*", number, '*');
  }
}
