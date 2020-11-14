package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.DurationPickerSkin;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.function.Consumer;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.layout.Region;
import javafx.util.Callback;
import javafx.util.Pair;

public class DurationPicker extends Control {

    public DurationPicker() {
        getStyleClass().addAll("duration-picker", "text-input");

        setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        minimumDurationProperty().addListener(it -> {
            final Duration minimumDuration = getMinimumDuration();
            if (minimumDuration == null) {
                throw new IllegalArgumentException("the minimum duration can not be null, it always has to be at least Duration.ZERO");
            }
            if (minimumDuration.isNegative()) {
                throw new IllegalArgumentException("the minimum duration can not be negative, but was " + minimumDuration);
            }
        });

        setSeparatorFactory(pair -> {
            Label label = new Label(":");
            label.getStyleClass().add("separator");
            return label;
        });

        setOnShowPopup(picker -> show());

        getFields().setAll(ChronoUnit.DAYS, ChronoUnit.HOURS, ChronoUnit.MINUTES, ChronoUnit.SECONDS, ChronoUnit.MILLIS);

        MapChangeListener<? super Object, ? super Object> propertiesListener = change -> {
            if (change.wasAdded()) {
                if (change.getKey().equals("TIME_PICKER_POPUP")) {
                    setShowing(!isShowing());
                    getProperties().remove("TIME_PICKER_POPUP");
                } else if (change.getKey().equals("NEW_DURATION")) {
                    setDuration((Duration) change.getValueAdded());
                    getProperties().remove("NEW_DURATION");
                }
            }
        };

        getProperties().addListener(propertiesListener);
    }

    @Override
    public String getUserAgentStylesheet() {
        return DurationPicker.class.getResource("duration-picker.css").toExternalForm();
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new DurationPickerSkin(this);
    }

    private final ReadOnlyBooleanWrapper showing = new ReadOnlyBooleanWrapper(this, "showing", false);

    public final boolean isShowing() {
        return showing.get();
    }

    /**
     * A flag used to signal whether the popup should be showing itself or not.
     *
     * @return true if the popup should be showing
     */
    public final ReadOnlyBooleanProperty showingProperty() {
        return showing.getReadOnlyProperty();
    }

    private final void setShowing(boolean showing) {
        this.showing.set(showing);
    }

    /**
     * Forces the popup for hour and minute selection to show itself.
     */
    public final void show() {
        setShowing(true);
    }

    /**
     * Forces the popup for hour and minute selection to hide itself.
     */
    public final void hide() {
        setShowing(false);
    }

    private final ObjectProperty<Callback<Pair<ChronoUnit, ChronoUnit>, Node>> separatorFactory = new SimpleObjectProperty<>(this, "separatorFactory");

    public final Callback<Pair<ChronoUnit, ChronoUnit>, Node> getSeparatorFactory() {
        return separatorFactory.get();
    }

    /**
     * The separator factory is used to create nodes that will be placed between two fields
     * of the picker. E.g. to separate hours one would return a label with a colon in it (8 hours
     * 35 minutes and 40 seconds would then look like this -> "8:35:40").
     *
     * @return the separator factory
     */
    public ObjectProperty<Callback<Pair<ChronoUnit, ChronoUnit>, Node>> separatorFactoryProperty() {
        return separatorFactory;
    }

    public void setSeparatorFactory(Callback<Pair<ChronoUnit, ChronoUnit>, Node> separatorFactory) {
        this.separatorFactory.set(separatorFactory);
    }

    // duration

    private final ObjectProperty<Duration> duration = new SimpleObjectProperty<>(this, "duration", Duration.ZERO);

    public final Duration getDuration() {
        return duration.get();
    }

    public final ObjectProperty<Duration> durationProperty() {
        return duration;
    }

    public final void setDuration(Duration duration) {
        this.duration.set(duration);
    }

    // fields support

    private final ListProperty<ChronoUnit> fields = new SimpleListProperty<>(this, "fields", FXCollections.observableArrayList(ChronoUnit.HOURS, ChronoUnit.MINUTES, ChronoUnit.SECONDS));

    public final ObservableList<ChronoUnit> getFields() {
        return fields.get();
    }

    /**
     * The list of fields that will be displayed inside the control. Supported units are:
     * weeks, days, hours, minutes, seconds, millis.
     */
    public final ListProperty<ChronoUnit> fieldsProperty() {
        return fields;
    }

    public final void setFields(ObservableList<ChronoUnit> fields) {
        this.fields.set(fields);
    }

    // linking fields

    private final BooleanProperty linkingFields = new SimpleBooleanProperty(this, "linkingFields", true);

    public final boolean isLinkingFields() {
        return linkingFields.get();
    }

    /**
     * A property used to control whether the fields should automatically increase or decrease
     * the previous field when they reach their upper or lower limit.
     *
     * @return true if rollover is desired
     */
    public final BooleanProperty linkingFieldsProperty() {
        return linkingFields;
    }

    public final void setLinkingFields(boolean linkingFields) {
        this.linkingFields.set(linkingFields);
    }

    // rollover

    private final BooleanProperty rollover = new SimpleBooleanProperty(this, "rollOver", true);

    public final boolean isRollover() {
        return rollover.get();
    }

    /**
     * A flag used to signal whether the time fields should start at the beginning of its value range
     * when it reaches the end of it. E.g. incrementing hour 23 would result in hour 0 when the user tries
     * to increase it by one.
     *
     * @return true if the fields should rollover
     */
    public final BooleanProperty rolloverProperty() {
        return rollover;
    }

    public final void setRollover(boolean rollover) {
        this.rollover.set(rollover);
    }

    // popup

    private final ObjectProperty<Consumer<DurationPicker>> onShowPopup = new SimpleObjectProperty<>(this, "onShowPopup");

    public final Consumer<DurationPicker> getOnShowPopup() {
        return onShowPopup.get();
    }

    /**
     * This consumer will be invoked to bring up a control for entering the
     * time without using the keyboard. The default implementation shows a popup.
     *
     * @return the "on show popup" consumer
     */
    public final ObjectProperty<Consumer<DurationPicker>> onShowPopupProperty() {
        return onShowPopup;
    }

    public final void setOnShowPopup(Consumer<DurationPicker> onShowPopup) {
        this.onShowPopup.set(onShowPopup);
    }

    // popup trigger button

    private final BooleanProperty showPopupTriggerButton = new SimpleBooleanProperty(this, "showPopupTriggerButton", true);

    public final boolean getShowPopupTriggerButton() {
        return showPopupTriggerButton.get();
    }

    /**
     * Determines if the control will show a button for showing or hiding the
     * popup.
     *
     * @return true if the control will show a button for showing the popup
     */
    public final BooleanProperty showPopupTriggerButtonProperty() {
        return showPopupTriggerButton;
    }

    public final void setShowPopupTriggerButton(boolean showPopupTriggerButton) {
        this.showPopupTriggerButton.set(showPopupTriggerButton);
    }

    // minimum duration

    private final ObjectProperty<Duration> minimumDuration = new SimpleObjectProperty<>(this, "earliestTime", Duration.ZERO);

    public final Duration getMinimumDuration() {
        return minimumDuration.get();
    }

    /**
     * Stores the minimum duration that the picker can display. The minimum duration can not
     * be negative.
     *
     * @return the minimum duration
     */
    public final ObjectProperty<Duration> minimumDurationProperty() {
        return minimumDuration;
    }

    public final void setMinimumDuration(Duration minimumDuration) {
        this.minimumDuration.set(minimumDuration);
    }

    // maximum duration

    private final ObjectProperty<Duration> maximumDuration = new SimpleObjectProperty<>(this, "maximumDuration");

    public final Duration getMaximumDuration() {
        return maximumDuration.get();
    }

    /**
     * Stores the maximum duration that the picker can display.
     *
     * @return the maximum duration
     */
    public final ObjectProperty<Duration> maximumDurationProperty() {
        return maximumDuration;
    }

    public final void setMaximumDuration(Duration maximumDuration) {
        this.maximumDuration.set(maximumDuration);
    }

    // show units

    private final BooleanProperty showUnits = new SimpleBooleanProperty(this, "showUnits", true);

    public final boolean isShowUnits() {
        return showUnits.get();
    }

    public final BooleanProperty showUnitsProperty() {
        return showUnits;
    }

    public final void setShowUnits(boolean showUnits) {
        this.showUnits.set(showUnits);
    }

    // fill digits

    private final BooleanProperty fillDigits = new SimpleBooleanProperty(this, "fillDigits", true);

    public final boolean isFillDigits() {
        return fillDigits.get();
    }

    /**
     * Determines if the fields will be "filled" with leading zeros or not, example: "04" for
     * 4 hours, or "0005" for 5 milliseconds. This only applies to fields with a granularity
     * of HOURS or lower. It does not make sense to fill DAYS with it as there is no limit on
     * the number of days (no upper bound). The default value is "true".
     *
     * @return true if the fields will be filled with leading zeros
     */
    public final BooleanProperty fillDigitsProperty() {
        return fillDigits;
    }

    public final void setFillDigits(boolean fillDigits) {
        this.fillDigits.set(fillDigits);
    }

    // use abbreviations

    private final BooleanProperty displayShortLabel = new SimpleBooleanProperty(this, "displayShortLabel", true);

    public final boolean isDisplayShortLabel() {
        return displayShortLabel.get();
    }

    /**
     * Controls whether the labels used for the units will be abbreviated or not, example:
     * "days" or "d", "hours" or "h".
     *
     * @return true if labels will be short
     */
    public final BooleanProperty displayShortLabelProperty() {
        return displayShortLabel;
    }

    public final void setDisplayShortLabel(boolean displayShortLabel) {
        this.displayShortLabel.set(displayShortLabel);
    }
}
