import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

public class FitopiaHomeInterface extends JFrame {

    private static final Color APP_BG = new Color(243, 247, 247);
    private static final Color SURFACE = new Color(255, 255, 255);
    private static final Color PRIMARY = new Color(15, 118, 110);
    private static final Color PRIMARY_DARK = new Color(11, 84, 79);
    private static final Color TEXT_DARK = new Color(17, 24, 39);
    private static final Color TEXT_MUTED = new Color(75, 85, 99);
    private static final Color BORDER = new Color(220, 227, 230);

    public FitopiaHomeInterface() {
        setTitle("Fitopia - Home Interface (Java)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 760));
        setSize(1280, 860);
        setLocationRelativeTo(null);
        setContentPane(buildRootPanel());
    }

    private JPanel buildRootPanel() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(APP_BG);
        root.add(buildTopBar(), BorderLayout.NORTH);
        root.add(buildScrollableContent(), BorderLayout.CENTER);
        return root;
    }

    private JPanel buildTopBar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(new Color(8, 44, 43));
        top.setBorder(new EmptyBorder(14, 24, 14, 24));

        JLabel logo = new JLabel("FITOPIA");
        logo.setForeground(Color.WHITE);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 22));

        JPanel nav = new JPanel();
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.X_AXIS));
        nav.add(buildNavLabel("Home"));
        nav.add(buildNavLabel("Services"));
        nav.add(buildNavLabel("Plans"));
        nav.add(buildNavLabel("Blog"));
        nav.add(buildNavLabel("Contact"));
        nav.add(Box.createHorizontalStrut(10));
        nav.add(buildButton("Login", false));

        top.add(logo, BorderLayout.WEST);
        top.add(nav, BorderLayout.EAST);
        return top;
    }

    private JLabel buildNavLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(new Color(221, 242, 241));
        label.setBorder(new EmptyBorder(0, 16, 0, 0));
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return label;
    }

    private JScrollPane buildScrollableContent() {
        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(APP_BG);
        content.setBorder(new EmptyBorder(18, 18, 24, 18));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 16, 0);

        int row = 0;
        addSection(content, gbc, row++, buildHeroSection());
        addSection(content, gbc, row++, buildServicesSection());
        addSection(content, gbc, row++, buildAboutSection());
        addSection(content, gbc, row++, buildConsultationSection());
        addSection(content, gbc, row++, buildTestimonialsSection());
        addSection(content, gbc, row++, buildHowItWorksSection());
        addSection(content, gbc, row++, buildPlansSection());
        addSection(content, gbc, row++, buildBlogSection());
        addSection(content, gbc, row++, buildFooter());

        gbc.gridy = row;
        gbc.weighty = 1.0;
        content.add(new JPanel(), gbc);

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return scrollPane;
    }

    private void addSection(JPanel container, GridBagConstraints gbc, int row, JPanel section) {
        gbc.gridy = row;
        gbc.weighty = 0.0;
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.add(section, gbc);
    }

    private JPanel buildHeroSection() {
        GradientPanel hero = new GradientPanel();
        hero.setLayout(new BorderLayout(22, 0));
        hero.setBorder(new EmptyBorder(28, 28, 28, 28));
        hero.setPreferredSize(new Dimension(1000, 265));
        hero.setCornerRadius(22);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel badge = new JLabel("Fitopia Performance Platform");
        badge.setOpaque(true);
        badge.setBackground(new Color(255, 255, 255, 60));
        badge.setForeground(new Color(222, 252, 249));
        badge.setBorder(new EmptyBorder(5, 10, 5, 10));
        badge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        badge.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("<html><div style='width:560px;'>Train smarter, eat better, and stay consistent.</div></html>");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 42));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("<html><div style='width:560px;'>Build sustainable fitness habits with structured programs, personalized nutrition guidance, and progress tracking in one place.</div></html>");
        subtitle.setForeground(new Color(228, 235, 244));
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel ctaRow = new JPanel();
        ctaRow.setOpaque(false);
        ctaRow.setLayout(new BoxLayout(ctaRow, BoxLayout.X_AXIS));
        ctaRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        ctaRow.add(buildButton("Learn more", true));
        ctaRow.add(Box.createHorizontalStrut(10));
        ctaRow.add(buildButton("Contact us", false));

        left.add(badge);
        left.add(Box.createVerticalStrut(16));
        left.add(title);
        left.add(Box.createVerticalStrut(10));
        left.add(subtitle);
        left.add(Box.createVerticalStrut(20));
        left.add(ctaRow);

        JPanel right = new JPanel(new GridLayout(2, 1, 0, 12));
        right.setOpaque(false);
        right.add(buildStatCard("95%", "Goal completion rate"));
        right.add(buildStatCard("24/7", "Coach support"));
        right.setPreferredSize(new Dimension(220, 0));

        hero.add(left, BorderLayout.CENTER);
        hero.add(right, BorderLayout.EAST);
        return hero;
    }

    private JPanel buildStatCard(String value, String label) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(255, 255, 255, 34));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 255, 255, 90)),
            new EmptyBorder(14, 14, 14, 14)
        ));

        JLabel v = new JLabel(value);
        v.setForeground(Color.WHITE);
        v.setFont(new Font("Segoe UI", Font.BOLD, 28));

        JLabel l = new JLabel(label);
        l.setForeground(new Color(229, 246, 245));
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        card.add(v);
        card.add(Box.createVerticalStrut(5));
        card.add(l);
        return card;
    }

    private JPanel buildServicesSection() {
        JPanel panel = wrapSection("Services");
        panel.setLayout(new GridLayout(1, 3, 12, 0));
        panel.add(buildCard("Exercise Program", "Weekly routines tailored to your level and goals."));
        panel.add(buildCard("Nutrition Plans", "Simple meal structure to improve energy and recovery."));
        panel.add(buildCard("Diet Program", "Sustainable plans you can maintain long-term."));
        return panel;
    }

    private JPanel buildAboutSection() {
        JPanel panel = wrapSection("About");
        panel.setLayout(new GridLayout(1, 2, 14, 0));

        JPanel imagePlaceholder = new JPanel(new BorderLayout());
        imagePlaceholder.setBackground(new Color(213, 232, 232));
        imagePlaceholder.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            new EmptyBorder(12, 12, 12, 12)
        ));
        JLabel imageText = new JLabel("Coach Photo Placeholder", SwingConstants.CENTER);
        imageText.setForeground(new Color(47, 79, 79));
        imageText.setFont(new Font("Segoe UI", Font.BOLD, 16));
        imagePlaceholder.add(imageText, BorderLayout.CENTER);

        JPanel text = new JPanel();
        text.setBackground(SURFACE);
        text.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            new EmptyBorder(18, 18, 18, 18)
        ));
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel subtitle = new JLabel("Welcome to Fitopia");
        subtitle.setForeground(PRIMARY_DARK);
        subtitle.setFont(new Font("Segoe UI", Font.BOLD, 15));

        JLabel title = new JLabel("<html><div style='width:430px;'>Health care is a natural way of improving your life quality.</div></html>");
        title.setForeground(TEXT_DARK);
        title.setFont(new Font("Segoe UI", Font.BOLD, 27));

        JLabel desc = new JLabel("<html><div style='width:430px;'>Build stronger routines with practical coaching, nutrition, and support. This UI block maps the About section from your Symfony page.</div></html>");
        desc.setForeground(TEXT_MUTED);
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        text.add(subtitle);
        text.add(Box.createVerticalStrut(10));
        text.add(title);
        text.add(Box.createVerticalStrut(10));
        text.add(desc);
        text.add(Box.createVerticalStrut(16));
        text.add(buildButton("Discover Fitopia", true));

        panel.add(imagePlaceholder);
        panel.add(text);
        return panel;
    }

    private JPanel buildConsultationSection() {
        JPanel wrapper = wrapSection("Consultation");
        wrapper.setLayout(new BorderLayout(0, 12));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel title = new JLabel("Free Consultation");
        title.setForeground(TEXT_DARK);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        top.add(title, BorderLayout.WEST);
        wrapper.add(top, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(2, 3, 10, 10));
        form.setOpaque(false);
        form.add(buildField("First Name"));
        form.add(buildField("Last Name"));
        form.add(buildCombo());
        form.add(buildField("Date"));
        form.add(buildField("Time"));

        JButton book = buildButton("Book now", true);
        JPanel btnWrap = new JPanel(new BorderLayout());
        btnWrap.setOpaque(false);
        btnWrap.add(book, BorderLayout.CENTER);
        form.add(btnWrap);

        wrapper.add(form, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildTestimonialsSection() {
        JPanel panel = wrapSection("Testimonials");
        panel.setLayout(new BorderLayout(0, 12));

        JLabel title = new JLabel("Happy Clients & Feedbacks");
        title.setForeground(TEXT_DARK);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));

        JPanel grid = new JPanel(new GridLayout(1, 3, 10, 0));
        grid.setOpaque(false);
        grid.add(buildTestimonial("Racky Henderson", "Father", "Great coaching and very clear progress tracking."));
        grid.add(buildTestimonial("Henry Dee", "Businesswoman", "Simple routines, better consistency, better results."));
        grid.add(buildTestimonial("Mark Huff", "Entrepreneur", "The nutrition guidance made the biggest difference."));

        panel.add(title, BorderLayout.NORTH);
        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildHowItWorksSection() {
        JPanel panel = wrapSection("How It Works");
        panel.setLayout(new BorderLayout(0, 12));

        JLabel title = new JLabel("How it works?");
        title.setForeground(TEXT_DARK);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));

        JPanel steps = new JPanel(new GridLayout(1, 4, 10, 0));
        steps.setOpaque(false);
        steps.add(buildStep("01", "Follow the program"));
        steps.add(buildStep("02", "Work for result"));
        steps.add(buildStep("03", "Eat healthy food"));
        steps.add(buildStep("04", "Enjoy your life"));

        panel.add(title, BorderLayout.NORTH);
        panel.add(steps, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildPlansSection() {
        JPanel panel = wrapSection("Price & Plans");
        panel.setLayout(new BorderLayout(0, 12));

        JLabel title = new JLabel("Choose Your Perfect Plans");
        title.setForeground(TEXT_DARK);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));

        JPanel grid = new JPanel(new GridLayout(1, 4, 10, 0));
        grid.setOpaque(false);
        grid.add(buildPlanCard("Starter", 49));
        grid.add(buildPlanCard("Standard", 79));
        grid.add(buildPlanCard("Premium", 109));
        grid.add(buildPlanCard("Platinum", 159));

        panel.add(title, BorderLayout.NORTH);
        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildBlogSection() {
        JPanel panel = wrapSection("Blog");
        panel.setLayout(new BorderLayout(0, 12));

        JLabel title = new JLabel("Latest news from our blog");
        title.setForeground(TEXT_DARK);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));

        JPanel cards = new JPanel(new GridLayout(1, 3, 10, 0));
        cards.setOpaque(false);
        cards.add(buildBlogCard("January 30, 2020"));
        cards.add(buildBlogCard("January 30, 2020"));
        cards.add(buildBlogCard("January 30, 2020"));

        panel.add(title, BorderLayout.NORTH);
        panel.add(cards, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(new Color(16, 63, 63));
        footer.setBorder(new EmptyBorder(12, 16, 12, 16));
        JLabel text = new JLabel("Fitopia UI Mock - Interface only (No backend logic)", SwingConstants.CENTER);
        text.setForeground(new Color(225, 242, 241));
        text.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        footer.add(text, BorderLayout.CENTER);
        return footer;
    }

    private JPanel wrapSection(String name) {
        JPanel panel = new JPanel();
        panel.setBackground(SURFACE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            new EmptyBorder(14, 14, 14, 14)
        ));
        panel.setName(name);
        return panel;
    }

    private JPanel buildCard(String title, String text) {
        JPanel card = new JPanel();
        card.setBackground(SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            new EmptyBorder(16, 16, 16, 16)
        ));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel t = new JLabel(title);
        t.setForeground(TEXT_DARK);
        t.setFont(new Font("Segoe UI", Font.BOLD, 19));
        t.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel d = new JLabel("<html><div style='width:250px;'>" + text + "</div></html>");
        d.setForeground(TEXT_MUTED);
        d.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        d.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(t);
        card.add(Box.createVerticalStrut(8));
        card.add(d);
        return card;
    }

    private JPanel buildTestimonial(String name, String role, String text) {
        JPanel card = new JPanel();
        card.setBackground(SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            new EmptyBorder(14, 14, 14, 14)
        ));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel quote = new JLabel("\" " + text + " \"");
        quote.setForeground(TEXT_MUTED);
        quote.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JLabel n = new JLabel(name);
        n.setForeground(TEXT_DARK);
        n.setFont(new Font("Segoe UI", Font.BOLD, 16));

        JLabel r = new JLabel(role);
        r.setForeground(PRIMARY_DARK);
        r.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        card.add(quote);
        card.add(Box.createVerticalStrut(10));
        card.add(n);
        card.add(r);
        return card;
    }

    private JPanel buildStep(String number, String text) {
        JPanel card = new JPanel();
        card.setBackground(SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            new EmptyBorder(14, 14, 14, 14)
        ));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel n = new JLabel(number);
        n.setForeground(PRIMARY_DARK);
        n.setFont(new Font("Segoe UI", Font.BOLD, 24));
        n.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel t = new JLabel("<html><div style='text-align:center;width:180px;'>" + text + "</div></html>");
        t.setForeground(TEXT_DARK);
        t.setFont(new Font("Segoe UI", Font.BOLD, 15));
        t.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(n);
        card.add(Box.createVerticalStrut(8));
        card.add(t);
        return card;
    }

    private JPanel buildPlanCard(String name, int price) {
        JPanel card = new JPanel();
        card.setBackground(SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            new EmptyBorder(14, 14, 14, 14)
        ));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel title = new JLabel(name);
        title.setForeground(TEXT_DARK);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel p = new JLabel("$" + price);
        p.setForeground(PRIMARY_DARK);
        p.setFont(new Font("Segoe UI", Font.BOLD, 30));
        p.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel features = new JLabel("<html><div style='text-align:center;'>20 Workouts<br>Meal plans<br>One coaching<br>24/7 support</div></html>");
        features.setForeground(TEXT_MUTED);
        features.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        features.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton choose = buildButton("Choose", true);
        choose.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(title);
        card.add(Box.createVerticalStrut(8));
        card.add(p);
        card.add(Box.createVerticalStrut(8));
        card.add(features);
        card.add(Box.createVerticalStrut(10));
        card.add(choose);
        return card;
    }

    private JPanel buildBlogCard(String date) {
        JPanel card = new JPanel();
        card.setBackground(SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            new EmptyBorder(14, 14, 14, 14)
        ));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JPanel image = new JPanel();
        image.setBackground(new Color(217, 227, 233));
        image.setPreferredSize(new Dimension(100, 120));
        image.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        image.setBorder(BorderFactory.createLineBorder(new Color(205, 217, 224)));

        JLabel dateLabel = new JLabel(date);
        dateLabel.setForeground(PRIMARY_DARK);
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateLabel.setBorder(new EmptyBorder(8, 0, 0, 0));

        JLabel title = new JLabel("<html><div style='width:300px;'>Even the all-powerful Pointing has no control about the blind texts.</div></html>");
        title.setForeground(TEXT_DARK);
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));

        card.add(image);
        card.add(dateLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(title);
        return card;
    }

    private JTextField buildField(String placeholder) {
        JTextField field = new JTextField(placeholder);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            new EmptyBorder(8, 10, 8, 10)
        ));
        return field;
    }

    private JComboBox<String> buildCombo() {
        JComboBox<String> combo = new JComboBox<>(new String[] { "Services", "Exercise Program", "Nutrition Plans", "Diet Program" });
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        combo.setBorder(BorderFactory.createLineBorder(BORDER));
        return combo;
    }

    private JButton buildButton(String text, boolean filled) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBorder(new EmptyBorder(8, 14, 8, 14));

        if (filled) {
            button.setBackground(PRIMARY);
            button.setForeground(Color.WHITE);
            button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_DARK),
                new EmptyBorder(8, 14, 8, 14)
            ));
        } else {
            button.setBackground(Color.WHITE);
            button.setForeground(TEXT_DARK);
            button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(8, 14, 8, 14)
            ));
        }
        return button;
    }

    private static final class GradientPanel extends JPanel {
        private int cornerRadius = 18;

        private void setCornerRadius(int cornerRadius) {
            this.cornerRadius = cornerRadius;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            GradientPaint gradient = new GradientPaint(
                0, 0, new Color(2, 44, 44),
                0, getHeight(), new Color(11, 79, 82)
            );
            g2.setPaint(gradient);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Uses default look and feel if system one is unavailable.
            }
            new FitopiaHomeInterface().setVisible(true);
        });
    }
}
