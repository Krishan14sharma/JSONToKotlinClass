package com.mighty16.json.ui;

import com.intellij.openapi.ui.Messages;
import com.mighty16.json.core.models.ClassModel;
import com.mighty16.json.core.parser.SimpleParser;
import com.mighty16.json.resolver.KotlinDataClassResolver;

import org.json.JSONException;
import org.json.JSONObject;

import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class JSONEditDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField classNameTextField;
    private JTextPane jsonTestPanel;
    private JLabel jsonErrorLabel;
    private JSONColorizer jsonColorizer;
    private JSONEditCallbacks callbacks;
    private ErrorMessageParser errorMessageParser;
    private boolean isFormatting = false;

    private TextResources textResources;

    public JSONEditDialog(JSONEditCallbacks callbacks, TextResources resources) {
        this.callbacks = callbacks;
        this.textResources = resources;
        setContentPane(contentPane);
        setModal(true);
        setTitle(textResources.getJSONDialogTitle());
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> dispose());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        contentPane.registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        jsonTestPanel.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                if (!isFormatting) {
                    formatJson(jsonTestPanel.getText());
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (!isFormatting) {
                    formatJson(jsonTestPanel.getText());
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (!isFormatting) {
                    formatJson(jsonTestPanel.getText());
                }
            }
        });
        PopupListener popupListener = new PopupListener(GuiHelper.getJsonContextMenuPopup(jsonTestPanel, textResources));
        jsonTestPanel.addMouseListener(popupListener);

        jsonColorizer = new JSONColorizer(jsonTestPanel);
        errorMessageParser = new ErrorMessageParser();
    }

    private void onOK() {
        String text = jsonTestPanel.getText();
        if (text.isEmpty()) {
            Messages.showErrorDialog(textResources.getEmptyJSONMessage(),
                    textResources.getEmptyJSONTitle());
            return;
        }
        String className = classNameTextField.getText();
        if (className.isEmpty()) {
            Messages.showErrorDialog(textResources.getEmptyClassMessage(),
                    textResources.getEmptyClassNameTitle());
            return;
        }
        processJSON(text, className);
    }


    private void formatJson(String text) {
        if (text.length() == 0) {
            jsonErrorLabel.setText("");
            jsonColorizer.clearErrorHighLight();
            return;
        }
        if (isFormatting) {
            isFormatting = false;
            return;
        }
        isFormatting = true;
        Runnable doFormatting = () -> {
            try {
                JSONObject json = new JSONObject(text);
                int currentCaretPosition = jsonTestPanel.getCaretPosition();
                jsonTestPanel.setText(json.toString(4));
                jsonTestPanel.setCaretPosition(currentCaretPosition);
                jsonErrorLabel.setText("");
                jsonColorizer.clearErrorHighLight();
            } catch (JSONException jsonException) {
                String errorMessage = jsonException.getMessage();
                jsonErrorLabel.setText(errorMessage);

                ErrorMessageParser.ErrorLocation errorLocation = errorMessageParser.findErrorLocation(errorMessage);
                if (errorLocation != null) {
                    jsonColorizer.highlightError(errorLocation.line, errorLocation.character);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                jsonColorizer.colorize();
                isFormatting = false;
            }
        };
        SwingUtilities.invokeLater(doFormatting);
    }

    private void processJSON(String jsonText, String rootClassName) {
        try {
            SimpleParser parser = new SimpleParser(new KotlinDataClassResolver());
            JSONObject json = new JSONObject(jsonText);
            parser.parse(json, rootClassName);
            List<ClassModel> parsedClasses = parser.getClasses();

            dispose();
            if (callbacks != null) {
                callbacks.onJsonParsed(parsedClasses, rootClassName);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Messages.showErrorDialog(textResources.getJSONErrorMessage(e.getMessage()),
                    textResources.getJSONErrorTitle());
        } catch (Exception e) {
            e.printStackTrace();
            Messages.showErrorDialog(textResources.getErrorMessage(e.getMessage()),
                    textResources.getJSONErrorTitle());
        }
    }

    public interface JSONEditCallbacks {
        void onJsonParsed(List<ClassModel> classDataList, String rootClassName);
    }
}
