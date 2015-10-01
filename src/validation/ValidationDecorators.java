package validation;

import java.util.Arrays;
import java.util.function.Function;

import animation.AnimationType;
import animation.AnimationUtils;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Cell;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;

public class ValidationDecorators {

    private static final Double DEFAULT_TOOLTIP_OFFSET_X = 15.0;
    private static final Double DEFAULT_TOOLTIP_OFFSET_Y = 15.0;

    public static final String PROPERTY_VALIDATION_DECORATOR = "validation-decorator"; //NON-NLS
//    public static final String PROPERTY_REQUIRED_VALIDATION_DECORATOR = "required-validation-decorator"; //NON-NLS

//    public static Decorator<Label> graphicDecoratorCreator(Node targetNode, Decorator oldDecorator, ValidationEvent event) {
//        Label label;
//        String img = getValidationResultIcon(event.getEventType());
//        ImageView graphic = new ImageView(img);
//
//        if (oldDecorator != null && exists(targetNode, oldDecorator)) {
//            label = (Label) oldDecorator.getNode();
//            label.setGraphic(graphic);
//            label.setTooltip(createTooltip(event.getMessage(), label));
//            DecorationUtils.setAnimationPlayed(label, false);
//            return oldDecorator;
//        }
//        return createAndInstallValidationDecorator(targetNode, event, ValidationUtils.ValidationDecorationType.GRAPHIC);
//    }

    public static Decorator<Label> fontAwesomeDecoratorCreator(Node targetNode, Decorator oldDecorator, ValidationEvent event) {
        Label label;
        String fontAwesomeIcon = getValidationFontAwesomeIcon(event.getEventType());

        if (oldDecorator != null && exists(targetNode, oldDecorator)) {
            label = (Label) oldDecorator.getNode();
            label.setText(fontAwesomeIcon);
            label.setTooltip(createTooltip(event.getMessage(), label));
            DecorationUtils.setAnimationPlayed(label, false);
            return oldDecorator;
        } 
        return createAndInstallValidationDecorator(targetNode, event);
    }

    private static void defaultDecorationClickBehavior(MouseEvent mouseEvent) {
        if (mouseEvent.getTarget() != null && mouseEvent.getTarget() instanceof Label) {
            Label label = (Label) mouseEvent.getTarget();
            if (label.getTooltip() != null) {
                Point2D point = label.localToScene(DEFAULT_TOOLTIP_OFFSET_X, DEFAULT_TOOLTIP_OFFSET_Y);

                label.getTooltip().setAutoHide(true);
                label.getTooltip().show(label, point.getX()
                        + label.getScene().getX() + label.getScene().getWindow().getX(), point.getY()
                        + label.getScene().getY() + label.getScene().getWindow().getY());
            }
        }
    }

    private static boolean exists(Node targetNode, Decorator resultDecorator) {
        Object o = DecorationUtils.getDecorators(targetNode);
        if (o != null) 
            if (o.equals(resultDecorator) || (o instanceof Decorator[] && Arrays.asList((Decorator[]) o).contains(resultDecorator))) {
                return true;
        }
        return false;
    }

    private static ValidationUtils.TooltipFix createTooltip(String message, Label label) {
        ValidationUtils.TooltipFix tooltip = null;

        if (message != null && message.trim().length() > 0) {
            tooltip = new ValidationUtils.TooltipFix(label);
            tooltip.setText(message);
            tooltip.setAutoHide(true);
            tooltip.setConsumeAutoHidingEvents(false);
        }

        return tooltip;
    }

    private static Decorator<Label> createAndInstallValidationDecorator(Node targetNode, ValidationEvent event    				) {
        Label label = new Label();
        label.getStyleClass().add(PROPERTY_VALIDATION_DECORATOR);
        label.setTooltip(createTooltip(event.getMessage(), label));

//        switch (type) {
//            case GRAPHIC:
//                label.setGraphic(new ImageView(getValidationResultIcon(event.getEventType())));
//                break;
//            case FONT_AWESOME:
                label.setText(getValidationFontAwesomeIcon(event.getEventType()));
                label.setStyle("-fx-font-family: FontAwesome;");
//                break;
//        }

        label.addEventHandler(MouseEvent.MOUSE_CLICKED, ValidationDecorators::defaultDecorationClickBehavior);

        Decorator<Label> newDecorator = null;
        if (targetNode instanceof Cell) 
            newDecorator = new Decorator<>(label, Pos.CENTER_RIGHT, new Point2D(-60, 0), new Insets(0, 100, 0, 0), AnimationType.TADA);
         else 
            newDecorator = new Decorator<>(label, Pos.BOTTOM_LEFT, new Point2D(0, 0), new Insets(0, 0, 0, 0), AnimationType.TADA);
        
        DecorationUtils.install(targetNode, newDecorator);
        targetNode.getParent().requestLayout();
        return newDecorator;
    }

//    private static Decorator<Label> graphicRequiredCreator(Node targetNode) {
//        Image image = new Image(ICON_REQUIRED);
//        Label lblRequired = new Label("", new ImageView(image));
//        lblRequired.getStyleClass().add(PROPERTY_REQUIRED_VALIDATION_DECORATOR);
//
//        Decorator<Label> requiredDecorator = new Decorator<>(lblRequired, Pos.TOP_LEFT, new Point2D(image.getWidth()/2, image.getHeight()/2),
//                new Insets(0, 0, 0, 0), false, AnimationUtils.createTransition(targetNode, AnimationType.NONE));
//        return requiredDecorator;
//    }
//
//    private static Decorator<Label> fontAwesomeRequiredCreator(Node targetNode) {
//        Label lblRequired = new Label();
//        lblRequired.setText("\uf005");
//        lblRequired.setStyle("-fx-font-family: FontAwesome;");
//
//        lblRequired.getStyleClass().add(PROPERTY_REQUIRED_VALIDATION_DECORATOR);
//        Decorator<Label> requiredDecorator = new Decorator<>(lblRequired, Pos.TOP_LEFT, new Point2D(0, 0), new Insets(0, 0, 0, 0));
//        return requiredDecorator;
//    }
//
//    public static Decorator<Label> installRequiredDecorator(Node targetNode, Function<Node, Decorator<Label>> requiredDecoratorCreator) {
//        Decorator<Label> decorator = requiredDecoratorCreator.apply(targetNode);
//        DecorationUtils.install(targetNode, decorator);
//        targetNode.getParent().requestLayout();
//        return decorator;
//    }

    //-----------------------------------------------------------------------------------------------------
    public static final String ICON_CORRECT = "\uf00c"; //NON-NLS
    public static final String ICON_INFO = "\uf129"; //NON-NLS
    public static final String ICON_ATTENTION = "\uf12a"; //NON-NLS
    public static final String ICON_QUESTION = "\uf128"; //NON-NLS
    public static final String ICON_ERROR = "\uf00d"; //NON-NLS

      /**
     * By default, we use our own icons inside the JideFX but you can override this method to return another set of
     * icons and then call {@link #setInstance(ValidationFontAwesomeIcons)} to set your instance.
     *
     * @param type the event type
     * @return a full path to the icon in the class path format. For example, if you have an error.png icon under
     *         package com.mycompany.myicons, the full path would be "/com/mycompany/myicons/error.png". In the other
     *         word, the icon must be in the classpath in order for the icon to be used by {@code ValidationIcons}.
     */
    public static String getValidationFontAwesomeIcon(EventType<ValidationEvent> type) {
       if (ValidationEvent.VALIDATION_ERROR.equals(type))      return ICON_ERROR;
       if (ValidationEvent.VALIDATION_WARNING.equals(type))    return ICON_ATTENTION;
       if (ValidationEvent.VALIDATION_INFO.equals(type))       return ICON_INFO;
       return ICON_CORRECT;
    }
    public static final String ICON_REQUIRED = "decoration/required-indicator.png"; //NON-NLS
 
    /**
     * By default, we use our own icons inside the JideFX but you can override this method to return another set of
     * icons and then call {@link #setInstance(ValidationIcons)} to set your instance.
     *
     * @param type the event type
     * @return a full path to the icon in the class path format. For example, if you have an error.png icon under
     *         package com.mycompany.myicons, the full path would be "/com/mycompany/myicons/error.png". In the other
     *         word, the icon must be in the classpath in order for the icon to be used by {@code ValidationIcons}.
     */
    public static String getValidationResultIcon(EventType<ValidationEvent> type) {
        if (ValidationEvent.VALIDATION_ERROR.equals(type))           return ICON_ERROR;
        if (ValidationEvent.VALIDATION_WARNING.equals(type))          return ICON_ATTENTION;
        if (ValidationEvent.VALIDATION_INFO.equals(type))             return ICON_INFO;
       return ICON_CORRECT;
    }

}
