import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class SubscriptionManagerGUI extends JFrame {
    private static final Logger logger = LogManager.getLogger(SubscriptionManagerGUI.class);

    private SubscriptionManager manager;
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField searchField;

    public SubscriptionManagerGUI() {
        try {
            logger.info("Launching SubscriptionManager GUI...");

            List<Subscription> initialSubs = List.of(
                    new ProductSubscription("S0001", "Sophie Tness", "99119911", "09/25/2025", "$35.00", "Monthly", "In Progress"),
                    new ServiceSubscription("S0002", "Dan Durance", "99112233", "09/25/2025", "$35.00", "Monthly", "In Progress")
            );
            manager = new SubscriptionManager(initialSubs);

            setTitle("Subscription Manager");
            setSize(900, 400);
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            setLayout(new BorderLayout());

            tableModel = new DefaultTableModel(new String[]{"ID", "Customer", "Phone", "Next Date", "Recurring", "Plan", "Status", "Type"}, 0);
            table = new JTable(tableModel);
            add(new JScrollPane(table), BorderLayout.CENTER);

            JPanel topPanel = new JPanel(new FlowLayout());
            JButton addBtn = new JButton("Add");
            JButton editBtn = new JButton("Edit");
            JButton removeBtn = new JButton("Remove");
            searchField = new JTextField(15);

            topPanel.add(addBtn);
            topPanel.add(editBtn);
            topPanel.add(removeBtn);
            topPanel.add(new JLabel("Search:"));
            topPanel.add(searchField);
            add(topPanel, BorderLayout.NORTH);

            addBtn.addActionListener(e -> {
                try {
                    logger.info("Clicked 'Add' button");
                    showForm(null);
                } catch (Exception ex) {
                    logger.error("Error when handling 'Add' button click", ex);
                    showErrorDialog("An error occurred while adding a subscription.");
                }
            });

            editBtn.addActionListener(e -> {
                try {
                    int row = table.getSelectedRow();
                    if (row != -1) {
                        String id = (String) tableModel.getValueAt(row, 0);
                        Subscription sub = manager.findById(id);
                        logger.info("Editing subscription with ID: {}", id);
                        showForm(sub);
                    } else {
                        logger.warn("Edit attempted with no row selected.");
                    }
                } catch (Exception ex) {
                    logger.error("Error when handling 'Edit' button click", ex);
                    showErrorDialog("An error occurred while editing the subscription.");
                }
            });

            removeBtn.addActionListener(e -> {
                try {
                    int row = table.getSelectedRow();
                    if (row != -1) {
                        String id = (String) tableModel.getValueAt(row, 0);
                        manager.removeSubscription(id);
                        logger.info("Removed subscription with ID: {}", id);
                        refreshTable();
                    } else {
                        logger.warn("Remove attempted with no row selected.");
                    }
                } catch (Exception ex) {
                    logger.error("Error when handling 'Remove' button click", ex);
                    showErrorDialog("An error occurred while removing the subscription.");
                }
            });

            searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                public void insertUpdate(javax.swing.event.DocumentEvent e) { safeRefreshTable(); }
                public void removeUpdate(javax.swing.event.DocumentEvent e) { safeRefreshTable(); }
                public void changedUpdate(javax.swing.event.DocumentEvent e) { safeRefreshTable(); }
            });

            refreshTable();

        } catch (Exception e) {
            logger.error("Failed to initialize SubscriptionManagerGUI", e);
            showErrorDialog("Fatal error during application startup.");
            System.exit(1);
        }
    }

    private void showForm(Subscription existing) {
        try {
            JTextField customer = new JTextField(existing != null ? existing.customer : "");
            JTextField phone = new JTextField(existing != null ? existing.phone : "");
            JTextField nextDate = new JTextField(existing != null ? existing.nextDate : "");
            JTextField recurring = new JTextField(existing != null ? existing.recurring : "");
            JComboBox<String> planBox = new JComboBox<>(new String[]{"Monthly", "Yearly"});
            JComboBox<String> statusBox = new JComboBox<>(new String[]{"In Progress", "Quotation", "Closed"});
            JComboBox<String> typeBox = new JComboBox<>(new String[]{"Product", "Service"});

            if (existing != null) {
                planBox.setSelectedItem(existing.plan);
                statusBox.setSelectedItem(existing.status);
                typeBox.setSelectedItem(existing.getType());
            }

            JPanel panel = new JPanel(new GridLayout(0, 1));
            panel.add(new JLabel("Customer:")); panel.add(customer);
            panel.add(new JLabel("Phone:")); panel.add(phone);
            panel.add(new JLabel("Next Date:")); panel.add(nextDate);
            panel.add(new JLabel("Recurring:")); panel.add(recurring);
            panel.add(new JLabel("Plan:")); panel.add(planBox);
            panel.add(new JLabel("Status:")); panel.add(statusBox);
            panel.add(new JLabel("Type:")); panel.add(typeBox);

            int result = JOptionPane.showConfirmDialog(this, panel, existing == null ? "Add Subscription" : "Edit Subscription", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                //  SERIAL NUMBER
                String serialNumber = existing == null ? "S" + String.format("%04d", manager.getSubscriptions().size() + 1) : existing.id;
                if (!serialNumber.matches("S\\d{4}")) {
                    showErrorDialog("Serial number must be in format S0001 (4 digits after S).");
                    return;
                }

                //  CUSTOMER NAME
                if (customer.getText().trim().isEmpty()) {
                    showErrorDialog("Customer name cannot be empty.");
                    return;
                }

                // PHONE
                if (!phone.getText().matches("\\d{8}")) {
                    showErrorDialog("Phone number must be exactly 8 digits.");
                    return;
                }

                // RECURRING
                String recurringValue = recurring.getText().replace("$", "").trim();
                double price = 0;
                try {
                    price = Double.parseDouble(recurringValue);
                    if (price < 0) {
                        showErrorDialog("Recurring price cannot be negative.");
                        return;
                    }
                } catch (NumberFormatException ex) {
                    showErrorDialog("Recurring must be a valid number like $35.00");
                    return;
                }

                // DATE FORMAT
                try {
                    LocalDate.parse(nextDate.getText().trim(), DateTimeFormatter.ofPattern("MM/dd/yyyy"));
                } catch (DateTimeParseException ex) {
                    showErrorDialog("Date must be in format MM/dd/yyyy (e.g., 09/25/2025).");
                    return;
                }

                // ✅ Save
                if (existing == null) {
                    manager.addSubscription(customer.getText(), phone.getText(), nextDate.getText(), recurring.getText(),
                            planBox.getSelectedItem().toString(), statusBox.getSelectedItem().toString(), typeBox.getSelectedItem().toString());
                    logger.info("New subscription added for customer: {}", customer.getText());
                } else {
                    manager.editSubscription(existing.id, customer.getText(), phone.getText(), nextDate.getText(), recurring.getText(),
                            planBox.getSelectedItem().toString(), statusBox.getSelectedItem().toString(), typeBox.getSelectedItem().toString());
                    logger.info("Subscription with ID {} edited.", existing.id);
                }

                refreshTable();
            } else {
                logger.debug("Form cancelled by user.");
            }

        } catch (Exception ex) {
            logger.error("Error in showForm", ex);
            showErrorDialog("An error occurred while saving the subscription.");
        }
    }

    private void safeRefreshTable() {
        try {
            refreshTable();
        } catch (Exception ex) {
            logger.error("Error refreshing table", ex);
            showErrorDialog("An error occurred while refreshing the subscription list.");
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        String keyword = searchField.getText();
        List<Subscription> list = keyword.isEmpty() ? manager.getSubscriptions() : manager.filter(keyword);
        for (Subscription s : list) {
            tableModel.addRow(s.toRow());
        }
        logger.debug("Table refreshed. {} subscriptions shown.", list.size());
    }

    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        try {
            SwingUtilities.invokeLater(() -> {
                new SubscriptionManagerGUI().setVisible(true);
                logger.info("Application window visible.");
            });
        } catch (Exception e) {
            logger.error("Unhandled exception in main", e);
        }
    }
}
