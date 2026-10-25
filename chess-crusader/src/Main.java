import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;


public class Main extends JFrame implements Board {

    JLayeredPane layeredPane;

    JButton width4;
    JButton width8;
    ServerSocket serverSocket;
    Socket socket;
    ServerSocket serverSocketChat;
    Socket socketChat;
    HostOrJoin hostOrJoin;
    String userName;
    private final String connectionHost;
    private Peace selectedPiece;
    private JLabel selectedMarker;
    private ObjectOutputStream gameOutput;
    private boolean gameOver;
    int turn = 0;

    public static void main(String[] args) {
        String host = args.length == 0 ? "localhost" : args[0];
        SwingUtilities.invokeLater(() -> new Main(host));
    }

    Main(String connectionHost) {

        super("Chess Crusader");
        this.connectionHost = connectionHost;
        setResizable(false);
        getContentPane().setPreferredSize(new Dimension(320, 640));
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        layeredPane = new JLayeredPane();
        layeredPane.setSize(new Dimension(320, 640));
        layeredPane.setOpaque(true);
        layeredPane.setVisible(true);
        layeredPane.setBackground(Color.BLACK);

        hostAndJoin();

        add(layeredPane);
        layeredPane.repaint();

    }

    public void Gameplay(Object[][] gameboard, ObjectInputStream input, ObjectOutputStream output,
                         ServerSocket serverSocket, Socket socket) {
        gameOutput = output;
        setupMouseListeners(gameboard);
        Thread receiver = new Thread(() -> {
            try {
                while (true) {
                    if (!"MOVE".equals(input.readUTF())) {
                        throw new IOException("Unknown game message");
                    }
                    int fromRow = input.readInt();
                    int fromCol = input.readInt();
                    int toRow = input.readInt();
                    int toCol = input.readInt();
                    SwingUtilities.invokeLater(() -> {
                        int remoteSide = hostOrJoin == HostOrJoin.host ? 1 : 0;
                        if (!gameOver && turn % 2 == remoteSide &&
                                isLegalMove(gameboard, fromRow, fromCol, toRow, toCol, remoteSide)) {
                            applyMove(gameboard, fromRow, fromCol, toRow, toCol);
                        } else if (!gameOver) {
                            showConnectionError("Received an invalid move.");
                        }
                    });
                }
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> {
                    if (!gameOver) showConnectionError("Connection to the other player was lost.");
                });
            }
        }, "game-moves");
        receiver.setDaemon(true);
        receiver.start();
    }

    private void setupMouseListeners(Object[][] gameboard) {
        for (Component component : layeredPane.getComponentsInLayer(0)) {
            Rectangle bounds = component.getBounds();
            component.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    handleSquareClick(gameboard, bounds.y / 80, bounds.x / 80);
                }
            });
        }
    }

    @Override
    public void gameBoard(String gameTiles, ObjectInputStream objectInputStream, ObjectOutputStream objectOutputStream, ServerSocket serverSocket, Socket socket) {

        switch (gameTiles) {
            case "1":

                getContentPane().setPreferredSize(new Dimension(800, 640));
                layeredPane.setSize(new Dimension(800, 640));
                pack();

                // Fill screen with tiles
                int x = 0;
                int y = 0;
                int width = 80;
                int height = 80;
                int numberOfTiles = 320 * 640 / (width * height);

                for (int i = 0; i < numberOfTiles; i++) {

                    // Add ground tiles
                    int row = y / height;
                    int col = x / width;
                    JLabel tileLabel = new JLabel();
                    if (row == 7) {

                        String assetindex = switch (col) {
                            case 0 -> "02";
                            case 1 -> "10";
                            case 2 -> "11";
                            case 3 -> "12";
                            default -> throw new IllegalStateException("Unexpected value: " + col);
                        };
                        ImageIcon tileImage = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("assets/" + assetindex + "_Chess Crusader.png")));
                        tileImage.setImage(tileImage.getImage().getScaledInstance(80, 80, 0));
                        tileLabel.setIcon(tileImage);
                        tileLabel.setBounds(x, y, width, height);

                        layeredPane.add(tileLabel, 0);
                    } else if (col == 0) {
                        ImageIcon tileImage = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("assets/" + "0" + String.valueOf(9 - row) + "_Chess Crusader.png")));
                        tileImage.setImage(tileImage.getImage().getScaledInstance(80, 80, 0));
                        tileLabel.setIcon(tileImage);
                        tileLabel.setBounds(x, y, width, height);

                        layeredPane.add(tileLabel, 0);
                    } else {
                        String assetindex;
                        if ((row % 2 == 0) == (col % 2 == 0)) {
                            assetindex = "01";
                        } else {
                            assetindex = "00";
                        }
                        ImageIcon tileImage = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("assets/" + assetindex + "_Chess Crusader.png")));
                        tileImage.setImage(tileImage.getImage().getScaledInstance(80, 80, 0));

                        tileLabel.setIcon(tileImage);
                        tileLabel.setBounds(x, y, width, height);

                        layeredPane.add(tileLabel, 0);
                    }

                    layeredPane.repaint(new Rectangle(x, y, width, height));

                    x += width;
                    if (x >= 320) {
                        x = 0;
                        y += height;
                    }
                }


                // Loop to add pieces to the board
                for (int i = 0; i < Gametiles1.length; i++) {
                    for (int j = 0; j < Gametiles1[i].length; j++) {
                        if (i == 0 && j == 0) {
                            addPieceToBoard(new Archer(2, 1, "cristian Archer", 0, 0, 0), j, i, 4);
                        } else if (i == 0 && j == 1) {
                            addPieceToBoard(new Castle(1, 1, "cristian Castle", 80, 0, 0), j, i, 4);
                        } else if (i == 0 && j == 2) {
                            addPieceToBoard(new Catapult(0, 1, "cristian Catapult", 160, 0, 0), j, i, 4);
                        } else if (i == 0 && j == 3) {
                            addPieceToBoard(new Knight(1, 2, "cristian Knight", 240, 0, 0), j, i, 4);
                        } else if (i == 1 && j == 0) {
                            addPieceToBoard(new Sarbaz(1, 1, "cristian Soldier", 0, 80, 0), j, i, 4);
                        } else if (i == 1 && j == 1) {
                            addPieceToBoard(new Sarbaz(1, 1, "cristian Soldier", 80, 80, 0), j, i, 4);
                        } else if (i == 1 && j == 2) {
                            addPieceToBoard(new Sarbaz(1, 1, "cristian Soldier", 160, 80, 0), j, i, 4);
                        } else if (i == 1 && j == 3) {
                            addPieceToBoard(new Sarbaz(1, 1, "cristian Soldier", 240, 80, 0), j, i, 4);
                        } else if (i == 6 && j == 0) {
                            addPieceToBoard(new Sarbaz(1, 1, "muslim Soldier", 0, 480, 1), j, i, 4);
                        } else if (i == 6 && j == 1) {
                            addPieceToBoard(new Sarbaz(1, 1, "muslim Soldier", 80, 480, 1), j, i, 4);
                        } else if (i == 6 && j == 2) {
                            addPieceToBoard(new Sarbaz(1, 1, "muslim Soldier", 160, 480, 1), j, i, 4);
                        } else if (i == 6 && j == 3) {
                            addPieceToBoard(new Sarbaz(1, 1, "muslim Soldier", 240, 480, 1), j, i, 4);
                        } else if (i == 7 && j == 0) {
                            addPieceToBoard(new Knight(1, 2, "muslim Knight", 0, 560, 1), j, i, 4);
                        } else if (i == 7 && j == 1) {
                            addPieceToBoard(new Catapult(0, 1, "muslim Catapult", 80, 560, 1), j, i, 4);
                        } else if (i == 7 && j == 2) {
                            addPieceToBoard(new Castle(1, 1, "muslim Castle", 160, 560, 1), j, i, 4);
                        } else if (i == 7 && j == 3) {
                            addPieceToBoard(new Archer(2, 1, "muslim Archer", 240, 560, 1), j, i, 4);
                        }
                    }
                }


                Peace.ability(Gametiles1);


                // adding peaces powers to the panel
                setPowerLable(Gametiles1);
                layeredPane.repaint();

                Gameplay(Gametiles1, objectInputStream, objectOutputStream, serverSocket, socket);

                break;
            case "2":

                getContentPane().setPreferredSize(new Dimension(1120, 640));
                pack();
                layeredPane.setSize(new Dimension(1120, 640));


                // Fill screen with tiles
                int x2 = 0;
                int y2 = 0;
                int width2 = 80;
                int height2 = 80;
                int numberOfTiles2 = 640 * 640 / (width2 * height2);

                for (int i2 = 0; i2 < numberOfTiles2; i2++) {

                    // Add ground tiles
                    int row2 = y2 / height2;
                    int col2 = x2 / width2;
                    JLabel tileLabel2;

                    if (row2 == 7) {

                        String assetindex2 = switch (col2) {
                            case 0 -> "02";
                            case 1 -> "10";
                            case 2 -> "11";
                            case 3 -> "12";
                            case 4 -> "36";
                            case 5 -> "37";
                            case 6 -> "38";
                            case 7 -> "39";
                            default -> throw new IllegalStateException("Unexpected value: " + col2);
                        };

                        ImageIcon tileImage2 = null;

                        if (col2 < 4) {
                            tileImage2 = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("assets/" + assetindex2 + "_Chess Crusader.png")));
                        } else {
                            tileImage2 = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("assets/" + assetindex2 + "_Chess Crusader.jpg")));
                        }

                        tileImage2.setImage(tileImage2.getImage().getScaledInstance(width2, height2, 0));
                        tileLabel2 = new JLabel(tileImage2);
                        tileLabel2.setBounds(x2, y2, width2, height2);

                        layeredPane.add(tileLabel2, 0);
                    } else if (col2 == 0) {
                        ImageIcon tileImage2 = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("assets/" + "0" + String.valueOf(9 - row2) + "_Chess Crusader.png")));
                        tileImage2.setImage(tileImage2.getImage().getScaledInstance(width2, height2, 0));
                        tileLabel2 = new JLabel(tileImage2);
                        tileLabel2.setBounds(x2, y2, width2, height2);

                        layeredPane.add(tileLabel2, 0);
                    } else {
                        String assetindex2;
                        if ((row2 % 2 == 0) == (col2 % 2 == 0)) {
                            assetindex2 = "01";
                        } else {
                            assetindex2 = "00";
                        }
                        ImageIcon tileImage2 = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("assets/" + assetindex2 + "_Chess Crusader.png")));
                        tileImage2.setImage(tileImage2.getImage().getScaledInstance(width2, height2, 0));
                        tileLabel2 = new JLabel(tileImage2);
                        tileLabel2.setBounds(x2, y2, width2, height2);

                        layeredPane.add(tileLabel2, 0);
                    }


                    layeredPane.repaint(new Rectangle(x2, y2, width2, height2));
                    x2 += width2;
                    if (x2 >= 640) {
                        x2 = 0;
                        y2 += height2;
                    }
                }


                for (int i = 0; i < Gametiles2.length; i++) {
                    for (int j = 0; j < Gametiles2[i].length; j++) {
                        if (i == 0 && j == 0) {
                            addPieceToBoard(new Archer(2, 1, "cristian Archer", 0, 0, 0), j, i, 8);
                        } else if (i == 0 && j == 1) {
                            addPieceToBoard(new Knight(1, 2, "cristian Knight", 80, 0, 0), j, i, 8);
                        } else if (i == 0 && j == 2) {
                            addPieceToBoard(new Archer(2, 1, "cristian Archer", 160, 0, 0), j, i, 8);
                        } else if (i == 0 && j == 3) {
                            addPieceToBoard(new Castle(1, 1, "cristian Castle", 240, 0, 0), j, i, 8);
                        } else if (i == 0 && j == 4) {
                            addPieceToBoard(new Catapult(0, 1, "cristian Catapult", 320, 0, 0), j, i, 8);
                        } else if (i == 0 && j == 5) {
                            addPieceToBoard(new Archer(2, 1, "cristian Archer", 400, 0, 0), j, i, 8);
                        } else if (i == 0 && j == 6) {
                            addPieceToBoard(new Knight(1, 2, "cristian Knight", 480, 0, 0), j, i, 8);
                        } else if (i == 0 && j == 7) {
                            addPieceToBoard(new Archer(2, 1, "cristian Archer", 560, 0, 0), j, i, 8);
                        } else if (i == 1 && j == 0) {
                            addPieceToBoard(new Sarbaz(1, 1, "cristian Soldier", 0, 80, 0), j, i, 8);
                        } else if (i == 1 && j == 1) {
                            addPieceToBoard(new Sarbaz(1, 1, "cristian Soldier", 80, 80, 0), j, i, 8);
                        } else if (i == 1 && j == 2) {
                            addPieceToBoard(new Sarbaz(1, 1, "cristian Soldier", 160, 80, 0), j, i, 8);
                        } else if (i == 1 && j == 3) {
                            addPieceToBoard(new Sarbaz(1, 1, "cristian Soldier", 240, 80, 0), j, i, 8);
                        } else if (i == 1 && j == 4) {
                            addPieceToBoard(new Sarbaz(1, 1, "cristian Soldier", 320, 80, 0), j, i, 8);
                        } else if (i == 1 && j == 5) {
                            addPieceToBoard(new Sarbaz(1, 1, "cristian Soldier", 400, 80, 0), j, i, 8);
                        } else if (i == 1 && j == 6) {
                            addPieceToBoard(new Sarbaz(1, 1, "cristian Soldier", 480, 80, 0), j, i, 8);
                        } else if (i == 1 && j == 7) {
                            addPieceToBoard(new Sarbaz(1, 1, "cristian Soldier", 560, 80, 0), j, i, 8);
                        } else if (i == 6 && j == 0) {
                            addPieceToBoard(new Sarbaz(1, 1, "muslim Soldier", 0, 480, 1), j, i, 8);
                        } else if (i == 6 && j == 1) {
                            addPieceToBoard(new Sarbaz(1, 1, "muslim Soldier", 80, 480, 1), j, i, 8);
                        } else if (i == 6 && j == 2) {
                            addPieceToBoard(new Sarbaz(1, 1, "muslim Soldier", 160, 480, 1), j, i, 8);
                        } else if (i == 6 && j == 3) {
                            addPieceToBoard(new Sarbaz(1, 1, "muslim Soldier", 240, 480, 1), j, i, 8);
                        } else if (i == 6 && j == 4) {
                            addPieceToBoard(new Sarbaz(1, 1, "muslim Soldier", 320, 480, 1), j, i, 8);
                        } else if (i == 6 && j == 5) {
                            addPieceToBoard(new Sarbaz(1, 1, "muslim Soldier", 400, 480, 1), j, i, 8);
                        } else if (i == 6 && j == 6) {
                            addPieceToBoard(new Sarbaz(1, 1, "muslim Soldier", 480, 480, 1), j, i, 8);
                        } else if (i == 6 && j == 7) {
                            addPieceToBoard(new Sarbaz(1, 1, "muslim Soldier", 560, 480, 1), j, i, 8);
                        } else if (i == 7 && j == 0) {
                            addPieceToBoard(new Archer(2, 1, "muslim Archer", 0, 560, 1), j, i, 8);
                        } else if (i == 7 && j == 1) {
                            addPieceToBoard(new Knight(1, 2, "muslim Knight", 80, 560, 1), j, i, 8);
                        } else if (i == 7 && j == 2) {
                            addPieceToBoard(new Archer(2, 1, "muslim Archer", 160, 560, 1), j, i, 8);
                        } else if (i == 7 && j == 3) {
                            addPieceToBoard(new Catapult(0, 1, "muslim Catapult", 240, 560, 1), j, i, 8);
                        } else if (i == 7 && j == 4) {
                            addPieceToBoard(new Castle(1, 1, "muslim Castle", 320, 560, 1), j, i, 8);
                        } else if (i == 7 && j == 5) {
                            addPieceToBoard(new Archer(2, 1, "muslim Archer", 400, 560, 1), j, i, 8);
                        } else if (i == 7 && j == 6) {
                            addPieceToBoard(new Knight(1, 2, "muslim Knight", 480, 560, 1), j, i, 8);
                        } else if (i == 7 && j == 7) {
                            addPieceToBoard(new Archer(2, 1, "muslim Archer", 560, 560, 1), j, i, 8);
                        }
                    }
                }

                Peace.ability(Gametiles2);

                // adding peaces powers to the panel
                setPowerLable(Gametiles2);
                layeredPane.repaint();

                Gameplay(Gametiles2, objectInputStream, objectOutputStream, serverSocket, socket);

                break;
        }

    }

    public void setPowerLable(Object[][] gameboard) {

        Component[] components = layeredPane.getComponentsInLayer(50);
        for (Component component : components) {
            layeredPane.remove(component);
        }
        layeredPane.repaint();

        for (int i = 0; i < gameboard.length; i++) {
            for (int j = 0; j < gameboard[i].length; j++) {
                if (gameboard[i][j] instanceof Peace) {
                    int power = ((Peace) gameboard[i][j]).getPower();
                    if (power == 0) {

                        ImageIcon powerImage = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("assets/" + 26 + "_Chess Crusader.png")));
                        powerImage.getImage().getScaledInstance(80, 80, 0);
                        JLabel powerLable = new JLabel();
                        powerLable.setIcon(powerImage);
                        powerLable.setBounds((j * 80), (i * 80), 80, 80);

                        layeredPane.add(powerLable, Integer.valueOf(50));
                        layeredPane.repaint();

                    } else if (power == 1) {

                        ImageIcon powerImage = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("assets/" + 27 + "_Chess Crusader.png")));
                        powerImage.getImage().getScaledInstance(80, 80, 0);
                        JLabel powerLable = new JLabel();
                        powerLable.setIcon(powerImage);
                        powerLable.setBounds((j * 80), (i * 80), 80, 80);

                        layeredPane.add(powerLable, Integer.valueOf(50));
                        layeredPane.repaint();

                    } else if (power == 2) {

                        ImageIcon powerImage = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("assets/" + 28 + "_Chess Crusader.png")));
                        powerImage.getImage().getScaledInstance(80, 80, 0);
                        JLabel powerLable = new JLabel();
                        powerLable.setIcon(powerImage);
                        powerLable.setBounds((j * 80), (i * 80), 80, 80);

                        layeredPane.add(powerLable, Integer.valueOf(50));
                        layeredPane.repaint();

                    } else if (power == 3) {

                        ImageIcon powerImage = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("assets/" + 29 + "_Chess Crusader.png")));
                        powerImage.getImage().getScaledInstance(80, 80, 0);
                        JLabel powerLable = new JLabel();
                        powerLable.setIcon(powerImage);
                        powerLable.setBounds((j * 80), (i * 80), 80, 80);

                        layeredPane.add(powerLable, Integer.valueOf(50));
                        layeredPane.repaint();

                    } else if (power == 4) {

                        ImageIcon powerImage = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("assets/" + 30 + "_Chess Crusader.png")));
                        powerImage.getImage().getScaledInstance(80, 80, 0);
                        JLabel powerLable = new JLabel();
                        powerLable.setIcon(powerImage);
                        powerLable.setBounds((j * 80), (i * 80), 80, 80);

                        layeredPane.add(powerLable, Integer.valueOf(50));
                        layeredPane.repaint();

                    } else if (power == 5) {

                        ImageIcon powerImage = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("assets/" + 31 + "_Chess Crusader.png")));
                        powerImage.getImage().getScaledInstance(80, 80, 0);
                        JLabel powerLable = new JLabel();
                        powerLable.setIcon(powerImage);
                        powerLable.setBounds((j * 80), (i * 80), 80, 80);

                        layeredPane.add(powerLable, Integer.valueOf(50));
                        layeredPane.repaint();

                    } else if (power == 6) {

                        ImageIcon powerImage = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("assets/" + 32 + "_Chess Crusader.png")));
                        powerImage.getImage().getScaledInstance(80, 80, 0);
                        JLabel powerLable = new JLabel();
                        powerLable.setIcon(powerImage);
                        powerLable.setBounds((j * 80), (i * 80), 80, 80);

                        layeredPane.add(powerLable, Integer.valueOf(50));
                        layeredPane.repaint();

                    } else if (power == 7) {

                        ImageIcon powerImage = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("assets/" + 33 + "_Chess Crusader.png")));
                        powerImage.getImage().getScaledInstance(80, 80, 0);
                        JLabel powerLable = new JLabel();
                        powerLable.setIcon(powerImage);
                        powerLable.setBounds((j * 80), (i * 80), 80, 80);

                        layeredPane.add(powerLable, Integer.valueOf(50));
                        layeredPane.repaint();

                    } else if (power == 8) {

                        ImageIcon powerImage = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("assets/" + 34 + "_Chess Crusader.png")));
                        powerImage.getImage().getScaledInstance(80, 80, 0);
                        JLabel powerLable = new JLabel();
                        powerLable.setIcon(powerImage);
                        powerLable.setBounds((j * 80), (i * 80), 80, 80);

                        layeredPane.add(powerLable, Integer.valueOf(50));
                        layeredPane.repaint();

                    } else if (power == 9) {

                        ImageIcon powerImage = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("assets/" + 35 + "_Chess Crusader.png")));
                        powerImage.getImage().getScaledInstance(80, 80, 0);
                        JLabel powerLable = new JLabel();
                        powerLable.setIcon(powerImage);
                        powerLable.setBounds((j * 80), (i * 80), 80, 80);

                        layeredPane.add(powerLable, Integer.valueOf(50));
                        layeredPane.repaint();

                    }

                }
            }
        }

    }

    public void hostAndJoin() {
        JTextField namefield = new JTextField();
        namefield.setToolTipText("Enter your name");
        namefield.setFont(new Font(null, Font.PLAIN, 20));
        namefield.setBackground(Color.lightGray);
        namefield.setBorder(BorderFactory.createRaisedBevelBorder());
        namefield.setBounds(60, 320, 200, 40);

        JButton Host = new JButton("Host");
        Host.setFocusable(true);
        Host.setFont(new Font(null, Font.PLAIN, 20));
        Host.setBackground(Color.lightGray);
        Host.setBorder(BorderFactory.createRaisedBevelBorder());
        Host.setBounds(60, 200, 200, 40);
        Host.setEnabled(false); // Initially disabled
        Host.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (serverSocket == null || serverSocket.isClosed()) {
                    try {
                        serverSocket = new ServerSocket(8080);
                        serverSocketChat = new ServerSocket(1010);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Socket socket = serverSocket.accept();
                                    Socket socketChat = serverSocketChat.accept();
                                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                                    objectOutputStream.flush();
                                    ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                                    ObjectOutputStream objectOutputStreamChat = new ObjectOutputStream(socketChat.getOutputStream());
                                    objectOutputStreamChat.flush();
                                    ObjectInputStream objectInputStreamChat = new ObjectInputStream(socketChat.getInputStream());

                                    // Update UI
                                    hostOrJoin = HostOrJoin.host;
                                    userName = namefield.getText();
                                    SwingUtilities.invokeLater(() -> {
                                        layeredPane.removeAll();
                                        chooseWidth(objectInputStream, objectOutputStream, objectInputStreamChat, objectOutputStreamChat);
                                        layeredPane.repaint();
                                    });

                                    SwingUtilities.invokeLater(() -> Host.setEnabled(false));
                                } catch (IOException ex) {
                                    JOptionPane.showMessageDialog(null, "Failed to start the server. Please try again.");
                                    ex.printStackTrace();
                                }
                            }
                        }).start();
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(null, "Failed to start the server. Please try again.");
                        ex.printStackTrace();
                    }
                }
            }
        });

        JButton Join = new JButton("Join");
        Join.setFocusable(true);
        Join.setFont(new Font(null, Font.PLAIN, 20));
        Join.setBackground(Color.lightGray);
        Join.setBorder(BorderFactory.createRaisedBevelBorder());
        Join.setBounds(60, 260, 200, 40);
        Join.setEnabled(false); // Initially disabled
        Join.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            socket = new Socket(connectionHost, 8080);
                            socketChat = new Socket(connectionHost, 1010);
                            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                            objectOutputStream.flush();
                            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                            ObjectOutputStream objectOutputStreamChat = new ObjectOutputStream(socketChat.getOutputStream());
                            objectOutputStreamChat.flush();
                            ObjectInputStream objectInputStreamChat = new ObjectInputStream(socketChat.getInputStream());

                            hostOrJoin = HostOrJoin.join;
                            userName = namefield.getText();
                            SwingUtilities.invokeLater(() -> {
                                layeredPane.removeAll();
                                JLabel waitLabel = new JLabel("Wait for the host to choose");
                                waitLabel.setFont(new Font(null, Font.PLAIN, 20));
                                waitLabel.setBackground(Color.lightGray);
                                waitLabel.setBorder(BorderFactory.createRaisedBevelBorder());
                                waitLabel.setBounds(33, 300, 250, 50);
                                waitLabel.setOpaque(true);
                                layeredPane.add(waitLabel);
                                layeredPane.repaint();
                            });

                            String width = objectInputStream.readUTF();
                            String finalWidth = width;
                            SwingUtilities.invokeLater(() -> {
                                layeredPane.removeAll();
                                layeredPane.repaint();
                                gameBoard(finalWidth, objectInputStream, objectOutputStream, serverSocket, socket);
                                String chatWidth = finalWidth.equals("1") ? "4" : "8";
                                new Chat(chatWidth, userName, objectOutputStreamChat, objectInputStreamChat, layeredPane);
                                JPanel panel = new JPanel();
                                panel.setBackground(Color.gray);
                                panel.setBounds(chatWidth.equals("4") ? 640 : 960, 0,
                                        chatWidth.equals("4") ? 160 : 240, 640);
                                layeredPane.add(panel);
                                new AudioControl().backMusic(panel);
                            });

                        } catch (IOException e) {
                            JOptionPane.showMessageDialog(null, "Failed to connect to server. Please try again.");
                            e.printStackTrace();
                        }
                    }

                }).start();
            }
        });

        namefield.getDocument().addDocumentListener(new DocumentListener() {
            private void updateButtons() {
                boolean hasName = !namefield.getText().isBlank();
                Host.setEnabled(hasName);
                Join.setEnabled(hasName);
            }
            @Override public void insertUpdate(DocumentEvent e) { updateButtons(); }
            @Override public void removeUpdate(DocumentEvent e) { updateButtons(); }
            @Override public void changedUpdate(DocumentEvent e) { updateButtons(); }
        });

        layeredPane.add(namefield);
        layeredPane.add(Host);
        layeredPane.add(Join);
        layeredPane.repaint();
    }

    public void chooseWidth(ObjectInputStream objectInputStream, ObjectOutputStream objectOutputStream, ObjectInputStream objectInputStreamChat, ObjectOutputStream objectOutputStreamChat) {
        width4 = new JButton("start with width = 4");
        width4.setFont(new Font(null, Font.PLAIN, 20));
        width4.setBackground(Color.lightGray);
        width4.setFocusable(true);
        width4.setBorder(BorderFactory.createRaisedBevelBorder());
        width4.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                layeredPane.removeAll();
                try {
                    objectOutputStream.writeUTF("1");
                    objectOutputStream.flush();
                } catch (IOException e3) {
                    e3.printStackTrace();
                }
                new Chat("4", userName, objectOutputStreamChat, objectInputStreamChat, layeredPane);
                gameBoard("1", objectInputStream, objectOutputStream, serverSocket, socket);

                JPanel panel = new JPanel();
                panel.setBackground(Color.gray);
                panel.setBounds(640, 0, 160, 640);
                layeredPane.add(panel);

                AudioControl audioControl = new AudioControl();
                audioControl.backMusic(panel);
            }
        });
        width4.setBounds(60, 200, 200, 40);

        width8 = new JButton("start with width = 8");
        width8.setFont(new Font(null, Font.PLAIN, 20));
        width8.setBackground(Color.lightGray);
        width8.setFocusable(true);
        width8.setBorder(BorderFactory.createRaisedBevelBorder());
        width8.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                layeredPane.removeAll();
                try {
                    objectOutputStream.writeUTF("2");
                    objectOutputStream.flush();
                } catch (IOException e3) {
                    e3.printStackTrace();
                }
                new Chat("8", userName, objectOutputStreamChat, objectInputStreamChat, layeredPane);
                gameBoard("2", objectInputStream, objectOutputStream, serverSocket, socket);

                JPanel panel = new JPanel();
                panel.setBackground(Color.gray);
                panel.setBounds(960, 0, 240, 640);
                layeredPane.add(panel);

                AudioControl audioControl = new AudioControl();
                audioControl.backMusic(panel);
            }
        });
        width8.setBounds(60, 260, 200, 40);

        layeredPane.add(width4);
        layeredPane.add(width8);
    }


    private void addPieceMouseListener(Peace piece, Object[][] gameboard) {
        piece.getPeaceLable().addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                handleSquareClick(gameboard, piece.getY() / 80, piece.getX() / 80);
            }
        });
    }

    private void handleSquareClick(Object[][] board, int row, int col) {
        if (gameOver || row < 0 || col < 0 || row >= board.length || col >= board[row].length) return;
        int localSide = hostOrJoin == HostOrJoin.host ? 0 : 1;
        if (turn % 2 != localSide) return;
        Peace target = (Peace) board[row][col];
        if (target != null && target.getWhichside() == localSide) {
            if (selectedPiece == target) {
                clearSelection();
            } else {
                clearSelection();
                selectedPiece = target;
                selectedMarker = new JLabel(loadImageIcon(13));
                selectedMarker.setBounds(col * 80, row * 80, 80, 80);
                layeredPane.add(selectedMarker, Integer.valueOf(10));
                layeredPane.repaint();
            }
            return;
        }
        if (selectedPiece == null) return;
        int fromRow = selectedPiece.getY() / 80;
        int fromCol = selectedPiece.getX() / 80;
        if (!isLegalMove(board, fromRow, fromCol, row, col, localSide)) return;
        try {
            gameOutput.writeUTF("MOVE");
            gameOutput.writeInt(fromRow);
            gameOutput.writeInt(fromCol);
            gameOutput.writeInt(row);
            gameOutput.writeInt(col);
            gameOutput.flush();
            applyMove(board, fromRow, fromCol, row, col);
        } catch (IOException e) {
            showConnectionError("Could not send the move.");
        }
    }

    private boolean isLegalMove(Object[][] board, int fromRow, int fromCol,
                                int toRow, int toCol, int side) {
        if (!Peace.isValidPosition(board, fromRow, fromCol) ||
                !Peace.isValidPosition(board, toRow, toCol) ||
                (fromRow == toRow && fromCol == toCol) ||
                !(board[fromRow][fromCol] instanceof Peace piece) ||
                piece.getWhichside() != side ||
                !Peace.isValidMove(piece, toRow, toCol)) return false;
        Peace target = (Peace) board[toRow][toCol];
        return target == null || (target.getWhichside() != side &&
                target.getPower() <= piece.getPower());
    }

    private void applyMove(Object[][] board, int fromRow, int fromCol, int toRow, int toCol) {
        Peace piece = (Peace) board[fromRow][fromCol];
        Peace captured = (Peace) board[toRow][toCol];
        clearSelection();
        if (captured != null) layeredPane.remove(captured.getPeaceLable());
        board[fromRow][fromCol] = null;
        board[toRow][toCol] = piece;
        piece.setX(toCol * 80);
        piece.setY(toRow * 80);
        piece.getPeaceLable().setBounds(toCol * 80, toRow * 80, 80, 80);
        Peace.ability(board);
        setPowerLable(board);
        turn++;
        layeredPane.repaint();
        if (captured instanceof Castle) {
            gameOver = true;
            String winner = piece.getWhichside() == 0 ? "Christian" : "Muslim";
            JOptionPane.showMessageDialog(this, winner + " wins!");
        }
    }

    private void clearSelection() {
        selectedPiece = null;
        if (selectedMarker != null) {
            layeredPane.remove(selectedMarker);
            selectedMarker = null;
            layeredPane.repaint();
        }
    }

    private void showConnectionError(String message) {
        gameOver = true;
        clearSelection();
        JOptionPane.showMessageDialog(this, message, "Game stopped", JOptionPane.ERROR_MESSAGE);
    }

    // Method to add a piece to the board
    private void addPieceToBoard(Peace piece, int x, int y, int gameWidth) {
        if (gameWidth == 4) {
            Gametiles1[y][x] = piece;
        } else {
            Gametiles2[y][x] = piece;
        }
        ImageIcon peaceImage = loadImageIcon(piece.getAssetIndex());
        piece.setImageIcon(peaceImage);
        piece.getPeaceLable().setIcon(piece.getImageIcon());
        piece.getPeaceLable().setBounds(piece.getX(), piece.getY(), 80, 80);
        layeredPane.add(piece.getPeaceLable(), Integer.valueOf(100));
        layeredPane.repaint();
        if (gameWidth == 4) {
            addPieceMouseListener(piece, Gametiles1);
        } else {
            addPieceMouseListener(piece, Gametiles2);
        }
    }

    // Method to load and scale image
    private ImageIcon loadImageIcon(int assetIndex) {
        ImageIcon peaceImage = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("assets/" + assetIndex + "_Chess Crusader.png")));
        peaceImage.getImage().getScaledInstance(80, 80, 0);
        return peaceImage;
    }

}
