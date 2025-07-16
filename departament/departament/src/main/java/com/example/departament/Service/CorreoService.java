package com.example.departament.Service;

import com.example.departament.Entity.Pago;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;
@Service
public class CorreoService {
    @Autowired
    private JavaMailSender mailSender;

    public void enviarContratoPorCorreo(String destinatario, File pdf) throws Exception {
        MimeMessage mensaje = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mensaje, true);

        helper.setTo(destinatario);
        helper.setSubject("Contrato de Propiedad");
        helper.setText("Estimado propietario, se adjunta el contrato en PDF.");

        FileSystemResource archivo = new FileSystemResource(pdf);
        helper.addAttachment("Contrato.pdf", archivo);

        mailSender.send(mensaje);
    }

    public void enviarBoletaPago(Pago pago, File boletaPdf) throws Exception {
        MimeMessage mensaje = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mensaje, true);

        helper.setTo(pago.getContrato().getPropietario().getCorreo());
        helper.setSubject("Boleta de pago - " + pago.getPeriodo());
        helper.setText("Estimado/a " + pago.getContrato().getPropietario().getNombres() + ",\n\nAdjunto encontrará su boleta de pago correspondiente al periodo " + pago.getPeriodo() + ".\n\nSaludos cordiales,\nAdministración del condominio");

        FileSystemResource archivo = new FileSystemResource(boletaPdf);
        helper.addAttachment("boleta_pago_" + pago.getId() + ".pdf", archivo);

        mailSender.send(mensaje);
    }
}
