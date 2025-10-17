package de.symeda.sormas.flow.views.ai;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import de.symeda.sormas.ai.api.AIOutbreakPredictionFacade;
import de.symeda.sormas.ai.api.dto.OutbreakPredictionDto;
import de.symeda.sormas.api.Disease;

/**
 * AI-Powered Outbreak Dashboard
 * Modern Vaadin Flow implementation
 */
@Route(value = "ai/outbreak-dashboard")
@PageTitle("AI Outbreak Dashboard - SORMAS")
public class AIOutbreakDashboard extends VerticalLayout {

    @Inject
    private AIOutbreakPredictionFacade aiPredictionFacade;

    private ComboBox<Disease> diseaseFilter;
    private VerticalLayout predictionsLayout;
    private Div summaryCard;

    @PostConstruct
    public void init() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // Header
        add(createHeader());

        // Filters
        add(createFilters());

        // Summary Cards
        add(createSummarySection());

        // Predictions Grid
        predictionsLayout = new VerticalLayout();
        predictionsLayout.setWidthFull();
        add(predictionsLayout);

        // Load initial data
        loadPredictions(null);
    }

    private Component createHeader() {
        H2 title = new H2("AI-Powered Outbreak Predictions");
        title.getStyle().set("margin", "0");

        Button refreshButton = new Button("Refresh", VaadinIcon.REFRESH.create());
        refreshButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        refreshButton.addClickListener(e -> refreshData());

        Button retrainButton = new Button("Retrain Models", VaadinIcon.COG.create());
        retrainButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        retrainButton.addClickListener(e -> triggerRetraining());

        HorizontalLayout header = new HorizontalLayout(title, refreshButton, retrainButton);
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);

        return header;
    }

    private Component createFilters() {
        diseaseFilter = new ComboBox<>("Disease");
        diseaseFilter.setItems(Disease.values());
        diseaseFilter.setPlaceholder("Select disease...");
        diseaseFilter.setWidthFull();
        diseaseFilter.addValueChangeListener(e -> loadPredictions(e.getValue()));

        HorizontalLayout filters = new HorizontalLayout(diseaseFilter);
        filters.setWidthFull();

        return filters;
    }

    private Component createSummarySection() {
        summaryCard = new Div();
        summaryCard.setWidthFull();
        summaryCard.getStyle()
            .set("display", "grid")
            .set("grid-template-columns", "repeat(auto-fit, minmax(250px, 1fr))")
            .set("gap", "1rem");

        return summaryCard;
    }

    private void loadPredictions(Disease disease) {
        try {
            List<OutbreakPredictionDto> predictions;

            if (disease != null) {
                predictions = aiPredictionFacade.getCurrentPredictions(disease);
            } else {
                // Load predictions for all diseases or default
                predictions = aiPredictionFacade.getCurrentPredictions(Disease.CORONAVIRUS);
            }

            updateSummary(predictions);
            updatePredictionsView(predictions);

        } catch (Exception e) {
            Notification.show("Error loading predictions: " + e.getMessage(),
                3000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void updateSummary(List<OutbreakPredictionDto> predictions) {
        summaryCard.removeAll();

        // Count by risk level
        long critical = predictions.stream()
            .filter(p -> p.getRiskLevel() == OutbreakPredictionDto.RiskLevel.CRITICAL)
            .count();

        long high = predictions.stream()
            .filter(p -> p.getRiskLevel() == OutbreakPredictionDto.RiskLevel.HIGH)
            .count();

        long moderate = predictions.stream()
            .filter(p -> p.getRiskLevel() == OutbreakPredictionDto.RiskLevel.MODERATE)
            .count();

        // Total predicted cases
        int totalPredictedCases = predictions.stream()
            .mapToInt(p -> p.getPredictedCases() != null ? p.getPredictedCases() : 0)
            .sum();

        // Create summary cards
        summaryCard.add(
            createSummaryCard("Critical Risk", String.valueOf(critical), "error"),
            createSummaryCard("High Risk", String.valueOf(high), "warning"),
            createSummaryCard("Moderate Risk", String.valueOf(moderate), "info"),
            createSummaryCard("Predicted Cases", String.valueOf(totalPredictedCases), "primary")
        );
    }

    private Component createSummaryCard(String title, String value, String theme) {
        Div card = new Div();
        card.getStyle()
            .set("padding", "1.5rem")
            .set("border-radius", "8px")
            .set("background", "var(--lumo-contrast-5pct)")
            .set("border-left", "4px solid var(--lumo-" + theme + "-color)");

        H3 cardTitle = new H3(title);
        cardTitle.getStyle().set("margin", "0 0 0.5rem 0");

        Span cardValue = new Span(value);
        cardValue.getStyle()
            .set("font-size", "2rem")
            .set("font-weight", "bold")
            .set("color", "var(--lumo-" + theme + "-text-color)");

        card.add(cardTitle, cardValue);
        return card;
    }

    private void updatePredictionsView(List<OutbreakPredictionDto> predictions) {
        predictionsLayout.removeAll();

        if (predictions.isEmpty()) {
            predictionsLayout.add(new Span("No predictions available"));
            return;
        }

        predictions.forEach(prediction -> {
            predictionsLayout.add(createPredictionCard(prediction));
        });
    }

    private Component createPredictionCard(OutbreakPredictionDto prediction) {
        Div card = new Div();
        card.setWidthFull();
        card.getStyle()
            .set("padding", "1.5rem")
            .set("border-radius", "8px")
            .set("background", "var(--lumo-contrast-5pct)")
            .set("margin-bottom", "1rem")
            .set("border-left", "4px solid " + getRiskLevelColor(prediction.getRiskLevel()));

        // Header
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);

        H3 regionTitle = new H3(prediction.getRegion().getCaption());
        regionTitle.getStyle().set("margin", "0");

        Span riskBadge = new Span(prediction.getRiskLevel().toString());
        riskBadge.getElement().getThemeList().add("badge");
        riskBadge.getElement().getThemeList().add(getRiskLevelTheme(prediction.getRiskLevel()));

        header.add(regionTitle, riskBadge);

        // Metrics
        HorizontalLayout metrics = new HorizontalLayout();
        metrics.setWidthFull();
        metrics.getStyle().set("margin-top", "1rem");

        metrics.add(
            createMetric("Risk Score", String.format("%.2f", prediction.getRiskScore() * 100) + "%"),
            createMetric("Predicted Cases", String.valueOf(prediction.getPredictedCases())),
            createMetric("Confidence", String.format("%.1f", prediction.getConfidence() * 100) + "%"),
            createMetric("Alert Level", prediction.getAlertLevel().toString())
        );

        // Recommendations
        if (prediction.getRecommendations() != null) {
            Div recommendations = new Div();
            recommendations.getStyle()
                .set("margin-top", "1rem")
                .set("padding", "1rem")
                .set("background", "var(--lumo-contrast-10pct)")
                .set("border-radius", "4px");

            Span recTitle = new Span("Recommendations:");
            recTitle.getStyle().set("font-weight", "bold");

            Span recText = new Span(prediction.getRecommendations());
            recText.getStyle().set("white-space", "pre-wrap");

            recommendations.add(recTitle, recText);
            card.add(header, metrics, recommendations);
        } else {
            card.add(header, metrics);
        }

        return card;
    }

    private Component createMetric(String label, String value) {
        VerticalLayout metric = new VerticalLayout();
        metric.setPadding(false);
        metric.setSpacing(false);

        Span labelSpan = new Span(label);
        labelSpan.getStyle()
            .set("font-size", "0.875rem")
            .set("color", "var(--lumo-secondary-text-color)");

        Span valueSpan = new Span(value);
        valueSpan.getStyle()
            .set("font-size", "1.25rem")
            .set("font-weight", "bold");

        metric.add(labelSpan, valueSpan);
        return metric;
    }

    private String getRiskLevelColor(OutbreakPredictionDto.RiskLevel level) {
        switch (level) {
            case CRITICAL: return "var(--lumo-error-color)";
            case HIGH: return "var(--lumo-warning-color)";
            case MODERATE: return "var(--lumo-primary-color)";
            case LOW: return "var(--lumo-success-color)";
            default: return "var(--lumo-contrast-50pct)";
        }
    }

    private String getRiskLevelTheme(OutbreakPredictionDto.RiskLevel level) {
        switch (level) {
            case CRITICAL: return "error";
            case HIGH: return "warning";
            case MODERATE: return "contrast";
            case LOW: return "success";
            default: return "normal";
        }
    }

    private void refreshData() {
        loadPredictions(diseaseFilter.getValue());
        Notification.show("Data refreshed", 2000, Notification.Position.BOTTOM_START)
            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void triggerRetraining() {
        try {
            String result = aiPredictionFacade.retrainModels();
            Notification.show(result, 5000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (Exception e) {
            Notification.show("Error triggering retraining: " + e.getMessage(),
                3000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}
