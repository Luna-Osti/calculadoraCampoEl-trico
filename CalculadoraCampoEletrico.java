package unespar.com.br.calculadoracampoeletrico;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CalculadoraCampoEletrico {

    static final double k = 8.99e9; // Constante eletrostática (N·m²/C²)
    static final double C = 1.6e-19; // Carga elementar (Coulombs) (a letra C é usada para que evite coincidências com certas funções)

    static Double Ex = 0.0; //Vetor Horizontal do Campo
    static Double Ey = 0.0; //Vetor Vertical do Campo
    static Double E = 0.0; //Modulo do Campo elétrico resultante
    static Double angT = 0.0; //Ângulo formado pelo campo resultante

    public static void main(String[] args) {

        //Formatação do layout
        JFrame painelPrincipal = new JFrame();
        painelPrincipal.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        painelPrincipal.setSize(800, 300);

        JPanel gridPrincipal = new JPanel(new GridBagLayout());
        JLabel labelCalculadora = new JLabel("Calculadora de Campo Elétrico");
        GridBagConstraints consLabel = new GridBagConstraints();
        consLabel.gridx = 1;
        consLabel.gridy = 0;
        gridPrincipal.add(labelCalculadora, consLabel);

        JPanel gridCargas = new JPanel(new GridLayout(6, 4));
        gridCargas.add(new JLabel("Dados:"));
        gridCargas.add(new JLabel("Carga (e)"));
        gridCargas.add(new JLabel("X (µm)"));
        gridCargas.add(new JLabel("Y (µm)"));

        JLabel labelCargaP = new JLabel("Ponto P:");
        JLabel labelCarga0 = new JLabel("0");
        JTextField fieldXP = new JTextField("0.0");
        JTextField fieldYP = new JTextField("0.0");
        gridCargas.add(labelCargaP);
        gridCargas.add(labelCarga0);
        gridCargas.add(fieldXP);
        gridCargas.add(fieldYP);

        JTextField[] fieldsC = new JTextField[4];
        JTextField[] fieldsX = new JTextField[4];
        JTextField[] fieldsY = new JTextField[4];

        for (int i = 0; i < 4; i++) {
            gridCargas.add(new JLabel("Carga " + (i + 1) + ":"));
            fieldsC[i] = new JTextField("0");
            fieldsX[i] = new JTextField("0.0");
            fieldsY[i] = new JTextField("0.0");
            gridCargas.add(fieldsC[i]);
            gridCargas.add(fieldsX[i]);
            gridCargas.add(fieldsY[i]);
        }

        GridBagConstraints consGrid = new GridBagConstraints();
        consGrid.fill = GridBagConstraints.HORIZONTAL;
        consGrid.gridx = 1;
        consGrid.gridy = 1;
        gridPrincipal.add(gridCargas, consGrid);

        JPanel linhaBotao = new JPanel(new FlowLayout());
        JButton botaoCalcular = new JButton("Calcular");
        JLabel labelResultado = new JLabel(String.format("E: %.2e N/C   Vetor E: %.2e i, %.2e j   Ângulo: %.2f°", E, Ex, Ey, angT));
        JButton botaoResetar = new JButton("Resetar");

        //Função do botão de calcular
        botaoCalcular.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    double xp_um = Double.parseDouble(fieldXP.getText());
                    double yp_um = Double.parseDouble(fieldYP.getText());

                    // Valida coordenadas do ponto P (entre -1.000.000 e 1.000.000)
                    if (xp_um < -1000000 || xp_um > 1000000 || yp_um < -1000000 || yp_um > 1000000) {
                        JOptionPane.showMessageDialog(null, "Coordenadas do ponto P devem estar entre -100000 e 100000 µm.", "Erro", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    //Conversão das medidas de µm para m
                    double xp = xp_um / 1e6;
                    double yp = yp_um / 1e6;

                    // Vetores para armazenar coordenadas das cargas
                    double[] xi_um = new double[4];
                    double[] yi_um = new double[4];
                    double[] xi = new double[4];
                    double[] yi = new double[4];
                    double[] qi = new double[4];

                    // Leitura e validação das cargas
                    for (int i = 0; i < 4; i++) {
                        int cargaInt = Integer.parseInt(fieldsC[i].getText());
                        if (cargaInt < -500 || cargaInt > 500) {
                            JOptionPane.showMessageDialog(null, "Carga " + (i + 1) + " deve estar entre -500 e 500.", "Erro", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        xi_um[i] = Double.parseDouble(fieldsX[i].getText());
                        yi_um[i] = Double.parseDouble(fieldsY[i].getText());

                        if (xi_um[i] < -1000000 || xi_um[i] > 1000000 || yi_um[i] < -1000000 || yi_um[i] > 1000000) {
                            JOptionPane.showMessageDialog(null, "Coordenadas da carga " + (i + 1) + " devem estar entre -100000 e 100000 µm.", "Erro", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        xi[i] = xi_um[i] / 1e6;
                        yi[i] = yi_um[i] / 1e6;
                        qi[i] = cargaInt * C;
                    }

                    // Checagem de coincidências
                    for (int i = 0; i < 4; i++) {
                        if (qi[i] == 0) continue;

                        if (xi_um[i] == xp_um && yi_um[i] == yp_um) {
                            JOptionPane.showMessageDialog(null, "Carga " + (i + 1) + " coincide com ponto P.", "Erro", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        for (int j = i + 1; j < 4; j++) {
                            if (qi[j] == 0) continue;
                            if (xi_um[i] == xi_um[j] && yi_um[i] == yi_um[j]) {
                                JOptionPane.showMessageDialog(null, "Carga " + (i + 1) + " coincide com carga " + (j + 1) + ".", "Erro", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                        }
                    }

                    // Calculação dos campos

                    for (int i = 0; i < 4; i++) {
                        if (qi[i] == 0) continue;

                        double dx = xp - xi[i]; //Distância x
                        double dy = yp - yi[i]; //Distância y
                        double r = Math.sqrt(dx * dx + dy * dy); //Distância vetorial

                        double Ei = k * qi[i] / (r * r);//Cálculo do módulo do campo elétrico
                        Ex += Ei * (dx / r);//Cálculo do vetor i do campo
                        Ey += Ei * (dy / r);//Cálculo do vetor j do campo
                    }

                    E = Math.sqrt(Ex * Ex + Ey * Ey);//Cáculo do módulo do campo elétrico RESULTANTE
                    angT = Math.toDegrees(Math.atan2(Ey, Ex));

                    labelResultado.setText(String.format("E: %.2e N/C   Vetor E: %.2e i, %.2e j   Ângulo: %.2f°", E, Ex, Ey, angT));

                    //Zera os vetores após o cálculo para futuras operações
                    Ex = 0.0;
                    Ey = 0.0;

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Erro: Verifique se todos os campos contêm valores numéricos válidos.", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        //Função do botão de resetar
        botaoResetar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fieldXP.setText("0.0");
                fieldYP.setText("0.0");
                for (int i = 0; i < 4; i++) {
                    fieldsC[i].setText("0");
                    fieldsX[i].setText("0.0");
                    fieldsY[i].setText("0.0");
                }
                E = 0.0;
                Ex = 0.0;
                Ey = 0.0;
                angT = 0.0;
                labelResultado.setText(String.format("E: %.2e N/C   Vetor E: %.2e i, %.2e j   Ângulo: %.2f°", E, Ex, Ey, angT));
            }
        });

        linhaBotao.add(botaoCalcular);
        linhaBotao.add(labelResultado);
        linhaBotao.add(botaoResetar);

        GridBagConstraints consBotao = new GridBagConstraints();
        consBotao.gridx = 1;
        consBotao.gridy = 2;
        gridPrincipal.add(linhaBotao, consBotao);

        painelPrincipal.add(gridPrincipal);
        painelPrincipal.setVisible(true);
    }
}


