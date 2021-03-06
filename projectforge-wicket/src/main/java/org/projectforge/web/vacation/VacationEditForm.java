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

package org.projectforge.web.vacation;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.business.configuration.ConfigurationService;
import org.projectforge.business.fibu.EmployeeDO;
import org.projectforge.business.fibu.EmployeeStatus;
import org.projectforge.business.fibu.api.EmployeeService;
import org.projectforge.business.multitenancy.TenantService;
import org.projectforge.business.user.UserRightId;
import org.projectforge.business.user.UserRightValue;
import org.projectforge.business.vacation.model.VacationDO;
import org.projectforge.business.vacation.model.VacationMode;
import org.projectforge.business.vacation.model.VacationStatus;
import org.projectforge.business.vacation.service.VacationService;
import org.projectforge.framework.access.AccessChecker;
import org.projectforge.framework.access.AccessException;
import org.projectforge.framework.i18n.I18nHelper;
import org.projectforge.framework.persistence.user.api.ThreadLocalUserContext;
import org.projectforge.web.employee.DefaultEmployeeWicketProvider;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.bootstrap.GridSize;
import org.projectforge.web.wicket.components.*;
import org.projectforge.web.wicket.flowlayout.*;
import org.slf4j.Logger;
import org.wicketstuff.select2.Select2Choice;

import java.math.BigDecimal;
import java.time.Year;
import java.util.Date;

public class VacationEditForm extends AbstractEditForm<VacationDO, VacationEditPage> {
  private static final long serialVersionUID = 8746545901236124484L;

  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(VacationEditForm.class);
  private final Model<String> neededVacationDaysModel = new Model<>();
  private final Model<String> availableVacationDaysModel = new Model<>();
  @SpringBean
  private VacationService vacationService;
  @SpringBean
  private EmployeeService employeeService;
  @SpringBean
  private TenantService tenantService;
  @SpringBean
  private ConfigurationService configService;
  @SpringBean
  private AccessChecker accessChecker;
  private Label neededVacationDaysLabel;
  private Label availableVacationDaysLabel;
  private VacationStatus statusBeforeModification;

  public VacationEditForm(final VacationEditPage parentPage, final VacationDO data) {
    super(parentPage, data);
    if (data.getEmployee() == null) {
      if (parentPage.employeeIdFromPageParameters != null) {
        data.setEmployee(employeeService.selectByPkDetached(parentPage.employeeIdFromPageParameters));
      } else {
        data.setEmployee(employeeService.getEmployeeByUserId(ThreadLocalUserContext.getUserId()));
      }
    }
    if (isNew() == false) {
      statusBeforeModification = data.getStatus();
    }
  }

  @Override
  public void onBeforeRender() {
    super.onBeforeRender();
    //Check write access
    //If status is approved only HR can make changes
    if ((isNew() == false && checkWriteAccess() == false) || (VacationStatus.APPROVED.equals(data.getStatus())
            && accessChecker.hasLoggedInUserRight(UserRightId.HR_VACATION, false, UserRightValue.READWRITE) == false)) {
      markAsDeletedButtonPanel.setVisible(false);
      deleteButtonPanel.setVisible(false);
      updateButtonPanel.setVisible(false);
    }
  }

  @Override
  protected void init() {
    super.init();
    if (checkReadAccess() == false) {
      throw new AccessException("access.exception.userHasNotRight");
    }
    VacationFormValidator formValidator = new VacationFormValidator(vacationService, vacationService, configService, data);
    add(formValidator);

    gridBuilder.newSplitPanel(GridSize.COL50);
    {
      // Employee
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("vacation.employee"));
      final Select2Choice<EmployeeDO> employeeSelect = new Select2Choice<>(
              Select2SingleChoicePanel.WICKET_ID,
              new PropertyModel<>(data, "employee"),
              new DefaultEmployeeWicketProvider(employeeService, true));
      employeeSelect.setRequired(true).setMarkupId("vacation-employee").setOutputMarkupId(true);
      employeeSelect.setEnabled(checkHRWriteRight());
      employeeSelect.add(new AjaxFormComponentUpdatingBehavior("change") {
        private static final long serialVersionUID = 2462231234993745889L;

        @Override
        protected void onUpdate(final AjaxRequestTarget target) {
          if (data.getEndDate() != null && data.getEndDate().getYear() == Year.now().getValue()) {
            BigDecimal availableVacationDays = getAvailableVacationDays(data);
            availableVacationDaysModel.setObject(availableVacationDays.toString());
            target.add(availableVacationDaysLabel);
          }
        }
      });
      formValidator.getDependentFormComponents()[3] = employeeSelect;
      fs.add(new Select2SingleChoicePanel<EmployeeDO>(fs.newChildId(), employeeSelect));
    }

    {
      // Start date
      final FieldsetPanel fs = gridBuilder.newFieldset(VacationDO.class, "startDate");
      LocalDatePanel startDatePanel = new LocalDatePanel(fs.newChildId(), new LocalDateModel(new PropertyModel<>(data, "startDate")),
              DatePanelSettings.get(), true);
      startDatePanel.setRequired(true).setMarkupId("vacation-startdate").setOutputMarkupId(true);
      startDatePanel.setEnabled(checkEnableInputField());
      formValidator.getDependentFormComponents()[0] = startDatePanel;
      fs.add(startDatePanel);

      // End date
      final FieldsetPanel fsEndDate = gridBuilder.newFieldset(VacationDO.class, "endDate");
      LocalDatePanel endDatePanel = new LocalDatePanel(fsEndDate.newChildId(), new LocalDateModel(new PropertyModel<>(data, "endDate")),
              DatePanelSettings.get().withTargetType(java.sql.Date.class), true);
      endDatePanel.getDateField().add(new AjaxFormComponentUpdatingBehavior("change") {
        private static final long serialVersionUID = 2462233112393745889L;

        @Override
        protected void onUpdate(final AjaxRequestTarget target) {
          updateNeededVacationDaysLabel(target);
        }
      });
      endDatePanel.setRequired(true).setMarkupId("vacation-enddate").setOutputMarkupId(true);
      endDatePanel.getDateField().setOutputMarkupId(true);
      endDatePanel.setEnabled(checkEnableInputField());
      formValidator.getDependentFormComponents()[1] = endDatePanel;
      fsEndDate.add(endDatePanel);

      startDatePanel.getDateField().add(new AjaxFormComponentUpdatingBehavior("change") {
        private static final long serialVersionUID = 4577664688930645961L;

        @Override
        protected void onUpdate(final AjaxRequestTarget target) {
          if (data.getEndDate() != null && data.getEndDate().getYear() == Year.now().getValue()) {
            // needed days
            BigDecimal availableVacationDays = getAvailableVacationDays(data);
            availableVacationDaysModel.setObject(availableVacationDays.toString());
            target.add(availableVacationDaysLabel);

            updateNeededVacationDaysLabel(target);
          }
          // end date
          final long selectedDate = startDatePanel.getDateField().getModelObject().getTime();

          if (endDatePanel.getDateField().getModelObject() == null) {
            endDatePanel.getDateField().setModelObject(new Date(selectedDate));
          } else if (selectedDate > endDatePanel.getDateField().getModelObject().getTime()) {
            endDatePanel.getDateField().getModelObject().setTime(selectedDate);
          }
          target.add(endDatePanel.getDateField());
        }
      });
    }

    {
      // half day checkbox
      final FieldsetPanel fs = gridBuilder.newFieldset(VacationDO.class, "halfDayBegin");
      final CheckBoxPanel checkboxPanel = new CheckBoxPanel(fs.newChildId(), new PropertyModel<>(data, "halfDayBegin"), "");
      checkboxPanel.setMarkupId("vacation-isHalfDay").setOutputMarkupId(true);
      checkboxPanel.setEnabled(checkEnableInputField());
      checkboxPanel.getCheckBox().add(new AjaxFormComponentUpdatingBehavior("change") {
        @Override
        protected void onUpdate(final AjaxRequestTarget target) {
          updateNeededVacationDaysLabel(target);
        }
      });
      formValidator.getDependentFormComponents()[4] = checkboxPanel.getCheckBox();
      fs.add(checkboxPanel);
    }

    {
      // Special holiday
      final FieldsetPanel fs = gridBuilder.newFieldset(VacationDO.class, "special");
      final CheckBoxPanel checkboxPanel = new CheckBoxPanel(fs.newChildId(), new PropertyModel<>(data, "special"), "");
      checkboxPanel.setMarkupId("vacation-special").setOutputMarkupId(true);
      checkboxPanel.setEnabled(checkEnableInputField());
      formValidator.getDependentFormComponents()[5] = checkboxPanel.getCheckBox();
      fs.add(checkboxPanel);
    }

    // Available vacation days - only visible for the creator and manager and from this Year
    if ((data.getVacationmode() == VacationMode.OWN ||
            data.getVacationmode() == VacationMode.MANAGER ||
            data.getVacationmode() == VacationMode.OTHER)
            &&
            (data.getEndDate() == null || data.getEndDate().getYear() == Year.now().getValue())
    ) {
      final FieldsetPanel fs = gridBuilder.newFieldset(I18nHelper.getLocalizedMessage("vacation.availabledays"));
      BigDecimal availableVacationDays = getAvailableVacationDays(data);
      this.availableVacationDaysModel.setObject(availableVacationDays.toString());
      LabelPanel availablePanel = new LabelPanel(fs.newChildId(), availableVacationDaysModel);
      availablePanel.setMarkupId("vacation-availableDays").setOutputMarkupId(true);
      this.availableVacationDaysLabel = availablePanel.getLabel();
      this.availableVacationDaysLabel.setOutputMarkupId(true);
      fs.add(availablePanel);
    }

    {
      // Needed vacation days
      FieldsetPanel neededVacationDaysFs = gridBuilder.newFieldset(I18nHelper.getLocalizedMessage("vacation.neededdays"));
      updateNeededVacationDaysLabel();
      LabelPanel neededVacationDaysPanel = new LabelPanel(neededVacationDaysFs.newChildId(), neededVacationDaysModel);
      neededVacationDaysPanel.setMarkupId("vacation-neededVacationDays").setOutputMarkupId(true);
      this.neededVacationDaysLabel = neededVacationDaysPanel.getLabel();
      this.neededVacationDaysLabel.setOutputMarkupId(true);

      neededVacationDaysFs.add(neededVacationDaysPanel);
    }

    {
      // Manager
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("vacation.manager"));
      final Select2Choice<EmployeeDO> managerSelect = new Select2Choice<>(
              Select2SingleChoicePanel.WICKET_ID,
              new PropertyModel<>(data, "manager"),
              new DefaultEmployeeWicketProvider(employeeService, checkHRWriteRight(), EmployeeStatus.FEST_ANGESTELLTER, EmployeeStatus.BEFRISTET_ANGESTELLTER,
                      EmployeeStatus.FREELANCER));
      managerSelect.setRequired(true).setMarkupId("vacation-manager").setOutputMarkupId(true);
      managerSelect.setEnabled(checkEnableInputField());
      fs.add(new Select2SingleChoicePanel<EmployeeDO>(fs.newChildId(), managerSelect));
    }

    {
      // Replacement
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("vacation.replacement"));
      final Select2Choice<EmployeeDO> replacementSelect = new Select2Choice<>(
              Select2SingleChoicePanel.WICKET_ID,
              new PropertyModel<>(data, "replacement"),
              new DefaultEmployeeWicketProvider(employeeService, checkHRWriteRight(), EmployeeStatus.FEST_ANGESTELLTER, EmployeeStatus.BEFRISTET_ANGESTELLTER,
                      EmployeeStatus.FREELANCER));
      replacementSelect.setRequired(true).setMarkupId("vacation-replacement").setOutputMarkupId(true);
      replacementSelect.setEnabled(checkEnableInputField());
      fs.add(new Select2SingleChoicePanel<EmployeeDO>(fs.newChildId(), replacementSelect));
    }

    {
      // DropDownChoice status
      final FieldsetPanel fs = gridBuilder.newFieldset(EmployeeDO.class, "status");
      final LabelValueChoiceRenderer<VacationStatus> statusChoiceRenderer = new LabelValueChoiceRenderer<>(
              this,
              VacationStatus.values());
      final DropDownChoice<VacationStatus> statusChoice = new DropDownChoice<>(fs.getDropDownChoiceId(),
              new PropertyModel<>(data, "status"), statusChoiceRenderer.getValues(), statusChoiceRenderer);
      statusChoice.setNullValid(false).setRequired(true);
      statusChoice.setMarkupId("vacation-status").setOutputMarkupId(true);
      statusChoice.setEnabled(hasUserEditStatusRight());
      formValidator.getDependentFormComponents()[2] = statusChoice;
      fs.add(statusChoice);
    }

    {
      // Comment
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("comment"));
      fs.add(new MaxLengthTextArea(TextAreaPanel.WICKET_ID, new PropertyModel<String>(data, "comment")), true);
    }
  }

  private void updateNeededVacationDaysLabel() {
    final BigDecimal days;
    if (data.getStartDate() == null || data.getEndDate() == null) {
      days = BigDecimal.ZERO;
    } else {
      days = vacationService.getVacationDays(data);
    }
    if (days != null) {
      neededVacationDaysModel.setObject(days.toString());
    } else {
      neededVacationDaysModel.setObject(I18nHelper.getLocalizedMessage("vacation.setStartAndEndFirst"));
    }
  }

  private void updateNeededVacationDaysLabel(final AjaxRequestTarget target) {
    updateNeededVacationDaysLabel();
    target.add(neededVacationDaysLabel);
  }

  private BigDecimal getAvailableVacationDays(VacationDO vacationData) {
    BigDecimal availableVacationDays;
    int year;
    if (vacationData.getStartDate() != null) {
      year = vacationData.getStartDate().getYear();
    } else {
      year = Year.now().getValue();
    }
    return vacationService.getVacationStats(vacationData.getEmployee(), year).getVacationDaysLeftInYear();
  }

  private boolean checkEnableInputField() {
    boolean result = false;
    if (data != null) {
      if (VacationStatus.APPROVED.equals(data.getStatus()) == true) {
        if (accessChecker.hasLoggedInUserRight(UserRightId.HR_VACATION, false, UserRightValue.READWRITE) == true) {
          result = true;
        }
      } else {
        if (data.getEmployee().getUser().getPk().equals(ThreadLocalUserContext.getUserId()) || accessChecker
                .hasLoggedInUserRight(UserRightId.HR_VACATION, false, UserRightValue.READWRITE)) {
          result = true;
        }
      }
    }
    return result;
  }

  private boolean hasUserEditStatusRight() {
    if (checkHRWriteRight()) {
      return true;
    }
    if (VacationStatus.APPROVED.equals(data.getStatus())) {
      //Only HR can change approved applications
      if (checkWriteAccess()) {
        return !data.getEmployee().getUser().getPk().equals(ThreadLocalUserContext.getUserId()) != true
                && data.getManager().getUser().getPk().equals(ThreadLocalUserContext.getUserId()) != true;
      }
    }
    //Only HR and manager can edit status when in progress
    if (checkWriteAccess()) {
      return data.getEmployee().getUser().getPk().equals(ThreadLocalUserContext.getUserId()) != true;
    }
    return false;
  }

  @Override
  protected Logger getLogger() {
    return log;
  }

  private boolean checkHRWriteRight() {
    return accessChecker.hasLoggedInUserRight(UserRightId.HR_VACATION, false, UserRightValue.READWRITE);
  }

  private boolean checkWriteAccess() {
    if (data.getEmployee().getUser().getPk().equals(ThreadLocalUserContext.getUserId()) == true || (data.getManager() != null
            && data.getManager().getUser().getPk().equals(ThreadLocalUserContext.getUserId())) == true) {
      return true;
    }
    return checkHRWriteRight();
  }

  private boolean checkReadAccess() {
    final Integer userId = ThreadLocalUserContext.getUserId();
    if (data.getEmployee().getUser().getPk().equals(userId)
            || (data.getManager() != null && data.getManager().getUser().getPk().equals(userId))
            || data.isReplacement(userId)) {
      return true;
    }
    return accessChecker.hasLoggedInUserRight(UserRightId.HR_VACATION, false, UserRightValue.READONLY,
            UserRightValue.READWRITE);
  }

  public VacationStatus getStatusBeforeModification() {
    return statusBeforeModification;
  }

  @Override
  protected void updateButtonVisibility() {
    super.updateButtonVisibility();
    //Set delete button only for employee or hr write right
    markAsDeletedButtonPanel.setVisible(false);
    if (data.getEmployee().getUser().getPk().equals(ThreadLocalUserContext.getUserId()) || checkHRWriteRight()) {
      markAsDeletedButtonPanel.setVisible(true);
    }
  }
}
