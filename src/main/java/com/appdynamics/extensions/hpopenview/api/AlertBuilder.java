package com.appdynamics.extensions.hpopenview.api;

import com.appdynamics.extensions.alerts.customevents.*;
import com.appdynamics.extensions.hpopenview.Configuration;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;

/**
 * Builds an Alert from Health Rule violation event.
 */

public class AlertBuilder {

    public static final String SLASH_SEPARATOR = "/";
    public static final String DASH = "-";
    private static Logger logger = Logger.getLogger(AlertBuilder.class);

    public Alert buildAlertFromHealthRuleViolationEvent(HealthRuleViolationEvent violationEvent, Configuration config) {
        if(violationEvent != null && config != null){
            Alert alert = new Alert();
            alert.setApplication(violationEvent.getAppName());
            alert.setNode(violationEvent.getAffectedEntityName());
            alert.setObject(violationEvent.getAffectedEntityName());
            setSeverity(violationEvent.getSeverity(),violationEvent);
            alert.setMsg_text(getSummary(violationEvent,true));
            return alert;
        }
        return null;
    }

    private void setSeverity(String severity, Event event) {
        if(severity.equalsIgnoreCase("WARN")){
            event.setSeverity("WARNING");
        }
        else if(severity.equalsIgnoreCase("ERROR")){
            event.setSeverity("CRITICAL");
        }
        else if(severity.equalsIgnoreCase("INFO")){
            event.setSeverity("INFO");
        }
    }



    public Alert buildAlertFromOtherEvent(OtherEvent otherEvent, Configuration config) {
        if (otherEvent != null && config != null) {
            Alert alert = new Alert();
            alert.setApplication(otherEvent.getAppName());
          //  alert.setNode(otherEvent.getAffectedEntityName());
          //  alert.setObject(otherEvent.getAffectedEntityName());
            setSeverity(otherEvent.getSeverity(),otherEvent);
            alert.setMsg_text(getSummary(otherEvent, true));
            return alert;
        }
        return null;
    }

    private String getDescription(OtherEvent otherEvent) {
        return "Event : " + otherEvent.getEventNotificationName() + " Severity: " + otherEvent.getSeverity();
    }

    private String getDescription(HealthRuleViolationEvent violationEvent) {
        return "Health Rule: " + violationEvent.getHealthRuleName() + " Severity: " + violationEvent.getSeverity();
    }

    private String getIncidentKey(HealthRuleViolationEvent violationEvent) {
        return violationEvent.getAppID() + DASH + violationEvent.getAffectedEntityID() + DASH + violationEvent.getHealthRuleID();
    }

    private String getIncidentKey(OtherEvent otherEvent) {
        return otherEvent.getAppID() + DASH + otherEvent.getEventNotificationId();
    }

    private String getEventTypes(OtherEvent otherEvent) {
        StringBuffer sb = new StringBuffer();
        for(EventType type : otherEvent.getEventTypes()){
            sb.append(type.getEventType());
            sb.append(",");
        }
        return sb.toString();
    }

    private String getEventSummaries(OtherEvent otherEvent) {
        StringBuffer sb = new StringBuffer();
        for(EventSummary summary : otherEvent.getEventSummaries()){
            sb.append(summary.getEventSummaryString());
            sb.append(",");
        }
        return sb.toString();
    }


    public String convertIntoJsonString(Alert alert) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(alert);
    }



    private String getAlertUrl(OtherEvent otherEvent) {
        if(otherEvent.getEventSummaries().get(0)  != null) {
            return otherEvent.getDeepLinkUrl() + otherEvent.getEventSummaries().get(0).getEventSummaryId();
        }
        return null;
    }



    private AlertDetails getSummary(HealthRuleViolationEvent violationEvent,boolean showDetails) {
        AlertHeatlhRuleVioEventDetails details = new AlertHeatlhRuleVioEventDetails();
        details.setApplicationId(violationEvent.getAppID());
        details.setApplicationName(violationEvent.getAppName());
        details.setPolicyViolationAlertTime(violationEvent.getPvnAlertTime());
        details.setSeverity(violationEvent.getSeverity());
        details.setPriority(violationEvent.getPriority());
        details.setHealthRuleName(violationEvent.getHealthRuleName());
        details.setAffectedEntityType(violationEvent.getAffectedEntityType());
        details.setAffectedEntityName(violationEvent.getAffectedEntityName());
        details.setIncidentId(violationEvent.getIncidentID());
        if(showDetails) {
            for (EvaluationEntity eval : violationEvent.getEvaluationEntity()) {
                AlertEvaluationEntity alertEval = buildAlertEvalutionEntity(eval);
                details.getEvaluationEntities().add(alertEval);
            }
        }
        return details;
    }

    private AlertDetails getSummary(OtherEvent otherEvent,boolean showDetails) {
        AlertOtherEventDetails details = new AlertOtherEventDetails();
        details.setApplicationId(otherEvent.getAppID());
        details.setApplicationName(otherEvent.getAppName());
        details.setEventNotificationIntervalInMins(otherEvent.getEventNotificationIntervalInMin());
        details.setSeverity(otherEvent.getSeverity());
        details.setPriority(otherEvent.getPriority());
        details.setEventNotificationName(otherEvent.getEventNotificationName());
        details.setEventNotificationId(otherEvent.getEventNotificationId());
        for(EventType eventType : otherEvent.getEventTypes()){
            AlertEventType alertEventType = new AlertEventType();
            alertEventType.setEventType(eventType.getEventType());
            alertEventType.setEventTypeNum(eventType.getEventTypeNum());
            details.getEventTypes().add(alertEventType);
        }
        if(showDetails) {
            for (EventSummary eventSummary : otherEvent.getEventSummaries()) {
                AlertEventSummary alertSummary = new AlertEventSummary();
                alertSummary.setEventSummaryId(eventSummary.getEventSummaryId());
                alertSummary.setEventSummaryTime(eventSummary.getEventSummaryTime());
                alertSummary.setEventSummaryType(eventSummary.getEventSummaryType());
                alertSummary.setEventSummarySeverity(eventSummary.getEventSummarySeverity());
                alertSummary.setEventSummaryString(eventSummary.getEventSummaryString());
                alertSummary.setEventSummaryDeepLinkUrl(otherEvent.getDeepLinkUrl() + alertSummary.getEventSummaryId());
                details.getEventSummaries().add(alertSummary);
            }
        }
        return details;
    }

    private AlertEvaluationEntity buildAlertEvalutionEntity(EvaluationEntity eval) {
        AlertEvaluationEntity alertEval = new AlertEvaluationEntity();
        alertEval.setName(eval.getName());
        alertEval.setId(eval.getId());
        alertEval.setType(eval.getType());
        alertEval.setNumberOfTriggeredConditions(eval.getNumberOfTriggeredConditions());
        for(TriggerCondition tc : eval.getTriggeredConditions()){
            AlertTriggeredCondition alertTrigger =  buildAlertTriggeredConditions(tc);
            alertEval.getTriggeredConditions().add(alertTrigger);
        }
        return alertEval;
    }

    private AlertTriggeredCondition buildAlertTriggeredConditions(TriggerCondition tc) {
        AlertTriggeredCondition alertTrigger = new AlertTriggeredCondition();
        alertTrigger.setScopeName(tc.getScopeName());
        alertTrigger.setScopeId(tc.getScopeId());
        alertTrigger.setScopeType(tc.getScopeType());
        alertTrigger.setConditionName(tc.getConditionName());
        alertTrigger.setConditionUnitType(tc.getConditionUnitType());
        alertTrigger.setConditionId(tc.getConditionId());
        alertTrigger.setBaselineId(tc.getBaselineId());
        alertTrigger.setBaselineName(tc.getBaselineName());
        alertTrigger.setUseDefaultBaseline(tc.isUseDefaultBaseline());
        alertTrigger.setOperator(tc.getOperator());
        alertTrigger.setObservedValue(tc.getObservedValue());
        alertTrigger.setThresholdValue(tc.getThresholdValue());
        return alertTrigger;
    }




    private String getEntityDisplayName(OtherEvent otherEvent) {
        return otherEvent.getAppName()  + SLASH_SEPARATOR + otherEvent.getEventNotificationName();
    }



}