package com.mighty16.json.ui;

import com.mighty16.json.core.LanguageResolver;
import com.mighty16.json.core.models.ClassModel;
import com.mighty16.json.core.models.FieldModel;

import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;

public class ModelTableDialog extends JDialog implements ClassesListDelegate.OnClassSelectedListener {

    private static final int ANNOTATIONS_GSON = 1;

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTable fieldsTable;
    private JTable table1;
    private JLabel claasesListLabel;

    private FieldsTableDelegate fieldsTableDelegate;
    private ClassesListDelegate classesListDelegate;

    private List<ClassModel> data;

    private ModelTableCallbacks callbacks;

    private int currentSelectedClassIndex = 0;

    private HashMap<String, String> classNames;

    private TextResources textResources;

    public ModelTableDialog(List<ClassModel> data, LanguageResolver resolver,
                            TextResources textResources, ModelTableCallbacks callbacks) {
        this.data = data;
        this.callbacks = callbacks;
        this.textResources = textResources;
        init();

        classNames = new HashMap<>();

        for (ClassModel classModel : data) {
            classNames.put(classModel.name, classModel.name);
        }

        classesListDelegate = new ClassesListDelegate(table1, data, classNames, this);
        fieldsTableDelegate = new FieldsTableDelegate(fieldsTable, resolver, textResources);
        fieldsTableDelegate.setClass(data.get(0));
        claasesListLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
    }

    private void init() {
        setContentPane(contentPane);
        setModal(true);
        setTitle(textResources.getFieldsDialogTitle());
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
    }


    private void onOK() {
        if (callbacks != null) {
            int annotationsType = ANNOTATIONS_GSON;
            for (ClassModel classModel : data) {
                String className = classNames.get(classModel.name);
                if (className != null) {
                    classModel.name = className;
                }
                Iterator<FieldModel> iterator = classModel.fields.iterator();
                while (iterator.hasNext()) {
                    FieldModel field = iterator.next();
                    if (!field.enabled) {
                        iterator.remove();
                    } else {
                        String fieldClassName = classNames.get(field.type);
                        if (fieldClassName != null) {
                            field.type = fieldClassName;
                        }
                    }
                }
            }
            callbacks.onModelsReady(data, "Data", annotationsType);
            dispose();
        }
    }

    @Override
    public void onClassSelected(ClassModel classData, int index) {
        data.get(currentSelectedClassIndex).fields = fieldsTableDelegate.getFieldsData();
        currentSelectedClassIndex = index;
        fieldsTableDelegate.setClass(classData);
    }

    public interface ModelTableCallbacks {
        void onModelsReady(List<ClassModel> data, String singleFileName, int annotationsType);
    }
}
