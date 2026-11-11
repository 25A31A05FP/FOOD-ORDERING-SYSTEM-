package com.foodordering.view;

import com.foodordering.controller.AppController;
import com.foodordering.model.User;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;

public class LoginFrame extends JFrame {
    private final AppController controller;
    private final Consumer<User> onLogin;
    private final CardLayout formLayout = new CardLayout();
    private final JPanel formCards = new JPanel(formLayout);
    private final JTextField loginEmail = new JTextField();
    private final JPasswordField loginPassword = new JPasswordField();
    private final JTextField registerName = new JTextField();
    private final JTextField registerEmail = new JTextField();
    private final JPasswordField registerPassword = new JPasswordField();

    public LoginFrame(AppController controller, Consumer<User> onLogin) {
        this.controller = controller;
        this.onLogin = onLogin;
        setTitle("Table & Thyme | Welcome");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 600));
        setSize(1080, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        add(createWelcomePanel(), BorderLayout.WEST);
        add(createForms(), BorderLayout.CENTER);
    }

    private JPanel createWelcomePanel() {
        JPanel welcome = new JPanel();
        welcome.setBackground(Theme.GREEN_DARK);
        welcome.setPreferredSize(new Dimension(390, 600));
        welcome.setBorder(BorderFactory.createEmptyBorder(48, 42, 42, 42));
        welcome.setLayout(new BoxLayout(welcome, BoxLayout.Y_AXIS));

        JLabel brand = new JLabel("TABLE & THYME");
        brand.setFont(Theme.font(14, Font.BOLD));
        brand.setForeground(new Color(193, 221, 193));
        JLabel headline = new JLabel("<html>Good food.<br>Good mood.</html>");
        headline.setFont(Theme.font(42, Font.BOLD));
        headline.setForeground(Color.WHITE);
        headline.setAlignmentX(LEFT_ALIGNMENT);
        JLabel description = new JLabel(
                "<html><div style='width:270px'>Thoughtfully made meals, fresh from our kitchen and ready for your table.</div></html>");
        description.setFont(Theme.font(15, Font.PLAIN));
        description.setForeground(new Color(224, 232, 224));
        description.setAlignmentX(LEFT_ALIGNMENT);
        JLabel note = new JLabel("<html><div style='width:270px'>Fresh ingredients &nbsp; · &nbsp; Made with care</div></html>");
        note.setFont(Theme.font(12, Font.PLAIN));
        note.setForeground(new Color(193, 221, 193));
        note.setAlignmentX(LEFT_ALIGNMENT);

        welcome.add(brand);
        welcome.add(Box.createVerticalStrut(112));
        welcome.add(headline);
        welcome.add(Box.createVerticalStrut(18));
        welcome.add(description);
        welcome.add(Box.createVerticalGlue());
        welcome.add(note);
        return welcome;
    }

    private JPanel createForms() {
        JPanel container = new JPanel(new GridBagLayout());
        container.setBackground(Theme.PAPER);
        container.add(formCards);
        formCards.setOpaque(false);
        formCards.add(createLoginForm(), "login");
        formCards.add(createRegistrationForm(), "register");
        return container;
    }

    private JPanel createLoginForm() {
        JPanel form = formBase();
        JPanel title = title("Welcome back", "Sign in to browse the menu and track your orders.");
        JButton signIn = Theme.button("Sign in", true);
        JButton registerLink = link("New here? Create an account");
        signIn.addActionListener(event -> signIn());
        registerLink.addActionListener(event -> formLayout.show(formCards, "register"));

        GridBagConstraints c = constraints();
        c.gridy = 0;
        form.add(title, c);
        c.gridy++;
        form.add(field("Email address", loginEmail), c);
        c.gridy++;
        form.add(field("Password", loginPassword), c);
        c.gridy++;
        signIn.setPreferredSize(new Dimension(330, 46));
        form.add(signIn, c);
        c.gridy++;
        form.add(registerLink, c);
        c.gridy++;
        JLabel privacy = new JLabel("Your password is securely hashed before it is saved.");
        privacy.setFont(Theme.font(11, Font.PLAIN));
        privacy.setForeground(Theme.MUTED);
        form.add(privacy, c);
        return form;
    }

    private JPanel createRegistrationForm() {
        JPanel form = formBase();
        JPanel title = title("Create your account", "A few details, then your next great meal is just a click away.");
        JButton create = Theme.button("Create account", true);
        JButton signInLink = link("Already have an account? Sign in");
        create.addActionListener(event -> register());
        signInLink.addActionListener(event -> formLayout.show(formCards, "login"));

        GridBagConstraints c = constraints();
        c.gridy = 0;
        form.add(title, c);
        c.gridy++;
        form.add(field("Full name", registerName), c);
        c.gridy++;
        form.add(field("Email address", registerEmail), c);
        c.gridy++;
        form.add(field("Password (8 characters minimum)", registerPassword), c);
        c.gridy++;
        create.setPreferredSize(new Dimension(330, 46));
        form.add(create, c);
        c.gridy++;
        form.add(signInLink, c);
        return form;
    }

    private JPanel formBase() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.LINE),
                BorderFactory.createEmptyBorder(32, 36, 32, 36)));
        return form;
    }

    private JPanel title(String heading, String subtitle) {
        JPanel labels = new JPanel();
        labels.setOpaque(false);
        labels.setLayout(new BoxLayout(labels, BoxLayout.Y_AXIS));
        JLabel headingLabel = new JLabel(heading);
        headingLabel.setFont(Theme.font(27, Font.BOLD));
        headingLabel.setForeground(Theme.INK);
        JLabel subtitleLabel = new JLabel("<html><div style='width:320px'>" + subtitle + "</div></html>");
        subtitleLabel.setFont(Theme.font(13, Font.PLAIN));
        subtitleLabel.setForeground(Theme.MUTED);
        labels.add(headingLabel);
        labels.add(Box.createVerticalStrut(9));
        labels.add(subtitleLabel);
        return labels;
    }

    private JPanel field(String label, javax.swing.JComponent input) {
        JPanel wrapper = new JPanel(new BorderLayout(0, 6));
        wrapper.setOpaque(false);
        JLabel caption = new JLabel(label);
        caption.setFont(Theme.font(12, Font.BOLD));
        caption.setForeground(Theme.INK);
        input.setPreferredSize(new Dimension(330, 42));
        input.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.LINE),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)));
        wrapper.add(caption, BorderLayout.NORTH);
        wrapper.add(input, BorderLayout.CENTER);
        wrapper.setPreferredSize(new Dimension(330, 70));
        return wrapper;
    }

    private JButton link(String text) {
        JButton link = new JButton(text);
        link.setFont(Theme.font(12, Font.BOLD));
        link.setForeground(Theme.GREEN);
        link.setHorizontalAlignment(SwingConstants.LEFT);
        link.setBorderPainted(false);
        link.setContentAreaFilled(false);
        link.setFocusPainted(false);
        return link;
    }

    private GridBagConstraints constraints() {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(8, 0, 8, 0);
        return c;
    }

    private void signIn() {
        char[] password = loginPassword.getPassword();
        runAsync(() -> controller.login(loginEmail.getText(), password).orElseThrow(
                () -> new IllegalArgumentException("The email or password is incorrect.")),
                user -> {
                    dispose();
                    onLogin.accept(user);
                }, password);
    }

    private void register() {
        char[] password = registerPassword.getPassword();
        runAsync(() -> controller.register(registerName.getText(), registerEmail.getText(), password),
                user -> {
                    dispose();
                    onLogin.accept(user);
                }, password);
    }

    private <T> void runAsync(java.util.concurrent.Callable<T> operation, Consumer<T> success, char[] password) {
        SwingWorker<T, Void> worker = new SwingWorker<>() {
            @Override
            protected T doInBackground() throws Exception {
                try {
                    return operation.call();
                } finally {
                    Arrays.fill(password, '\0');
                }
            }

            @Override
            protected void done() {
                try {
                    success.accept(get());
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    Theme.showError(LoginFrame.this.getRootPane(), exception);
                } catch (ExecutionException exception) {
                    Throwable cause = exception.getCause();
                    Theme.showError(LoginFrame.this.getRootPane(),
                            cause instanceof Exception appException
                                    ? appException : new Exception("Sign-in failed.", cause));
                }
            }
        };
        worker.execute();
    }
}
