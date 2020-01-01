/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2014 Kai Reinhard (k.reinhard@micromata.de)
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

package org.projectforge.web.fibu;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.business.fibu.*;
import org.projectforge.business.user.ProjectForgeGroup;
import org.projectforge.framework.access.OperationType;
import org.projectforge.framework.persistence.api.ModificationStatus;
import org.projectforge.framework.time.PFDay;
import org.projectforge.framework.utils.NumberHelper;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.AbstractSecuredBasePage;
import org.projectforge.web.wicket.EditPage;
import org.projectforge.web.wicket.WicketUtils;
import org.slf4j.Logger;

import java.time.LocalDate;

@EditPage(defaultReturnPage = AuftragListPage.class)
public class AuftragEditPage extends AbstractEditPage<AuftragDO, AuftragEditForm, AuftragDao>
    implements ISelectCallerPage
{
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AuftragEditPage.class);

  private static final long serialVersionUID = -8192471994161712577L;

  @SpringBean
  private AuftragDao auftragDao;

  @SpringBean
  private ProjektDao projektDao;

  public AuftragEditPage(final PageParameters parameters)
  {
    super(parameters, "fibu.auftrag");
    init();
    if (isNew() == true && getData().getContactPerson() == null) {
      auftragDao.setContactPerson(getData(), getUser().getId());
    }
  }

  @Override
  protected AuftragDao getBaseDao()
  {
    return auftragDao;
  }

  @Override
  protected AuftragEditForm newEditForm(final AbstractEditPage<?, ?, ?> parentPage, final AuftragDO data)
  {
    return new AuftragEditForm(this, data);
  }

  /**
   * @see org.projectforge.web.fibu.ISelectCallerPage#select(String, Object)
   */
  @Override
  public void select(final String property, final Object selectedValue)
  {
    if ("projektId".equals(property) == true) {
      auftragDao.setProjekt(getData(), (Integer) selectedValue);
      form.projektSelectPanel.getTextField().modelChanged();
      if (getData().getProjektId() != null && getData().getProjektId() >= 0) {
        final ProjektDO projekt = projektDao.getById(getData().getProjektId());
        form.setKundePmHobmAndSmIfEmpty(projekt, null);
      }
    } else if ("kundeId".equals(property) == true) {
      auftragDao.setKunde(getData(), (Integer) selectedValue);
      form.kundeSelectPanel.getTextField().modelChanged();
    } else if ("contactPersonId".equals(property) == true) {
      auftragDao.setContactPerson(getData(), (Integer) selectedValue);
      setSendEMailNotification();
    } else if (property.startsWith("taskId:") == true) {
      final Short number = NumberHelper.parseShort(property.substring(property.indexOf(':') + 1));
      final AuftragsPositionDO pos = getData().getPosition(number);
      auftragDao.setTask(pos, (Integer) selectedValue);
    } else {
      log.error("Property '" + property + "' not supported for selection.");
    }
  }

  private void setSendEMailNotification()
  {
    if (accessChecker.userEqualsToContextUser(getData().getContactPerson()) == true)
      form.setSendEMailNotification(false);
    else
      form.setSendEMailNotification(true);
  }

  /**
   * @see org.projectforge.web.fibu.ISelectCallerPage#unselect(java.lang.String)
   */
  @Override
  public void unselect(final String property)
  {
    if ("projektId".equals(property) == true) {
      getData().setProjekt(null);
      form.projektSelectPanel.getTextField().modelChanged();
    } else if ("kundeId".equals(property) == true) {
      getData().setKunde(null);
      form.kundeSelectPanel.getTextField().modelChanged();
    } else if ("contactPersonId".equals(property) == true) {
      getData().setContactPerson(null);
      setSendEMailNotification();
    } else if (property.startsWith("taskId:") == true) {
      final Short number = NumberHelper.parseShort(property.substring(property.indexOf(':') + 1));
      final AuftragsPositionDO pos = getData().getPosition(number);
      pos.setTask(null);
    } else {
      log.error("Property '" + property + "' not supported for selection.");
    }
  }

  /**
   * @see org.projectforge.web.fibu.ISelectCallerPage#cancelSelection(java.lang.String)
   */
  @Override
  public void cancelSelection(final String property)
  {
    // Do nothing.
  }

  @Override
  public AbstractSecuredBasePage onSaveOrUpdate()
  {
    if (getData().getNummer() == null) {
      getData().setNummer(auftragDao.getNextNumber(getData()));
    }
    if (getData().getKunde() != null) {
      getData().setKundeText(null);
    }
    return null;
  }

  @Override
  protected void onPreEdit()
  {
    final AuftragDO auftrag = getData();

    if (auftrag.getId() == null) {
      if (auftrag.getAngebotsDatum() == null) {
        final LocalDate today = LocalDate.now();
        auftrag.setAngebotsDatum(java.sql.Date.valueOf(today));
        auftrag.setErfassungsDatum(java.sql.Date.valueOf(today));
        auftrag.setEntscheidungsDatum(java.sql.Date.valueOf(today));
      }
      if (auftrag.getContactPersonId() == null && accessChecker.isLoggedInUserMemberOfGroup(ProjectForgeGroup.PROJECT_MANAGER)) {
        auftragDao.setContactPerson(auftrag, getUser().getId());
        form.setSendEMailNotification(false);
      }
    } else if (auftrag.getErfassungsDatum() == null) {
      if (auftrag.getCreated() != null) {
        auftrag.setErfassungsDatum(PFDay.from(auftrag.getCreated()).getSqlDate());
      } else if (auftrag.getAngebotsDatum() != null) {
        auftrag.setErfassungsDatum((java.sql.Date) auftrag.getAngebotsDatum().clone());
      } else {
        auftrag.setErfassungsDatum(java.sql.Date.valueOf(LocalDate.now()));
      }
    } else {
      setSendEMailNotification();
    }

    auftrag.recalculate();
  }

  @Override
  public AbstractSecuredBasePage afterSave()
  {
    if (form.isSendEMailNotification() == false) {
      return null;
    }
    sendNotificationIfRequired(OperationType.INSERT);
    return null;
  }

  @Override
  public AbstractSecuredBasePage afterUpdate(final ModificationStatus modified)
  {
    if (form.isSendEMailNotification() == false) {
      return null;
    }
    if (modified == ModificationStatus.MAJOR) {
      sendNotificationIfRequired(OperationType.UPDATE);
    }
    return null;
  }

  private void sendNotificationIfRequired(final OperationType operationType)
  {
    final String url = getPageAsLink(WicketUtils.getEditPageParameters(getData().getId()));
    auftragDao.sendNotificationIfRequired(getData(), operationType, url);
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
