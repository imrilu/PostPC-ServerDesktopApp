/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.rmi.Remote;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;

/**
 * @author proxc
 */
public class FXMLDocumentController implements Initializable {

    @FXML
    private ImageView btn_settings, btn_user, btn_exit, btn_qr;

    @FXML
    private ImageView qrView;

    @FXML
    private AnchorPane h_settings, h_user;

    protected static String QR_CODE_TEXT = "connectTo:";


    // h_user is the qr page. to be switched


    @FXML
    private void handleButtonAction(MouseEvent event) {
        if (event.getTarget() == btn_settings && !h_settings.isVisible()) {
            h_settings.setVisible(true);
            h_user.setVisible(false);
        } else if (event.getTarget() == btn_settings && h_settings.isVisible()) {
            h_settings.setVisible(!h_settings.isVisible());
        } else if (event.getTarget() == btn_qr && !h_user.isVisible()) {
            h_user.setVisible(true);
            h_settings.setVisible(false);
        } else if (event.getTarget() == btn_qr) {
            h_user.setVisible(!h_user.isVisible());
        } else if (event.getTarget() == btn_exit) {
            Platform.exit();
            System.exit(0);
        }

    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        QR_CODE_TEXT += RemoteDroidServer.getLocalIp();
        QR_CODE_TEXT += ":";
        QR_CODE_TEXT += RemoteDroidServer.SERVER_PORT;
        System.out.println("QR CODE: " + QR_CODE_TEXT);
        int width = 300;
        int height = 300;
        String fileType = "png";
        BufferedImage bufferedImage = null;
        try {
            BitMatrix byteMatrix = qrCodeWriter.encode(QR_CODE_TEXT, BarcodeFormat.QR_CODE, width, height);
            bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            bufferedImage.createGraphics();
            Graphics2D graphics = (Graphics2D) bufferedImage.getGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, width, height);
            graphics.setColor(Color.BLACK);
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    if (byteMatrix.get(i, j)) {
                        graphics.fillRect(i, j, 1, 1);
                    }
                }
            }
            System.out.println("Success...");
        } catch (WriterException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
        qrView.setImage(SwingFXUtils.toFXImage(bufferedImage, null));

        // setting the qr screen appear, and turning everything else off
        h_settings.setVisible(false);
        h_user.setVisible(true);

        // creating server socket in new thread
        new Thread(new Runnable() {
            public void run() {
                RemoteDroidServer.main();
            }
        }).start();

    }


}
