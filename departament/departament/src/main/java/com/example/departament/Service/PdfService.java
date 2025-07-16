package com.example.departament.Service;

import com.example.departament.Entity.Pago;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.example.departament.Entity.Contrato;
import com.itextpdf.layout.properties.TextAlignment;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
@Service
public class PdfService {
    /*public File generarContratoPdf(@NotNull Contrato contrato) throws Exception {
        String filePath = "C:/Users/Admin/Documents/contrato_" + contrato.getId() + ".pdf";
        File file = new File(filePath);
        PdfWriter writer = new PdfWriter(new FileOutputStream(file));
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);



            Font titleFont = new Font(Font.TYPE1_FONT(), 16, Font.BOLD);
            Font subTitleFont = new Font(Font.ITALIC, 14, Font.ROMAN_BASELINE);
            Font normalFont = new Font(Font.ITALIC, 12);
            Font boldFont = new Font(Font.ITALIC, 12, Font.BOLD);

            // Título
            Paragraph title = new Paragraph("CONTRATO DE ARRENDAMIENTO DE INMUEBLE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Conste por el presente documento privado, el Contrato de Arrendamiento de Inmueble que celebran de una parte:", normalFont));
            document.add(new Paragraph("La empresa [NOMBRE DE LA EMPRESA], en calidad de ARRENDADOR, representada por su administrador, y de la otra parte:", normalFont));
            document.add(new Paragraph(
                    contrato.getPropietario().getNombres() + " " + contrato.getPropietario().getApellidos()
                            + ", identificado con DNI N° " + contrato.getPropietario().getDni()
                            + ", con domicilio en el mismo edificio, quien actúa en calidad de ARRENDATARIO.", normalFont));

            document.add(new Paragraph(" "));

            document.add(new Paragraph("Las partes acuerdan lo siguiente:", boldFont));
            document.add(new Paragraph(" "));

            // CLÁUSULAS
            document.add(new Paragraph("CLÁUSULA PRIMERA: OBJETO DEL CONTRATO", subTitleFont));
            document.add(new Paragraph(
                    "El ARRENDADOR da en arrendamiento al ARRENDATARIO el departamento N° " +
                            contrato.getDepartamento().getNumero() + ", ubicado en el piso " + contrato.getDepartamento().getPiso() +
                            " del edificio perteneciente a la empresa, con un área de " + contrato.getDepartamento().getArea() + " m².", normalFont));

            document.add(new Paragraph(" "));

            document.add(new Paragraph("CLÁUSULA SEGUNDA: PLAZO DEL CONTRATO", subTitleFont));
            document.add(new Paragraph(
                    "El presente contrato tiene una duración de " + contrato.getCantidadMeses() + " meses, "
                            + "iniciando el " + contrato.getFechaInicio() + " y culminando el " + contrato.getFechaFin() + ".", normalFont));

            document.add(new Paragraph(" "));

            document.add(new Paragraph("CLÁUSULA TERCERA: MONTO DEL ARRENDAMIENTO", subTitleFont));
            document.add(new Paragraph(
                    "El ARRENDATARIO pagará un monto mensual de S/ "
                            + (contrato.getTipo() == Contrato.TipoContrato.ALQUILER
                            ? contrato.getMontoTotal() / contrato.getCantidadMeses()
                            : contrato.getMontoTotal()) +
                            ". El pago deberá realizarse dentro de los primeros cinco (5) días de cada mes.", normalFont));

            document.add(new Paragraph(" "));

            document.add(new Paragraph("CLÁUSULA CUARTA: DESTINO DEL INMUEBLE", subTitleFont));
            document.add(new Paragraph(
                    "El departamento será destinado exclusivamente a fines de vivienda. "
                            + "No podrá ser usado para fines comerciales ni subarrendado sin autorización escrita del ARRENDADOR.", normalFont));

            document.add(new Paragraph(" "));

            document.add(new Paragraph("CLÁUSULA QUINTA: OBLIGACIONES DEL ARRENDATARIO", subTitleFont));
            document.add(new Paragraph(
                    "- Conservar el inmueble en buen estado.\n" +
                            "- Permitir inspecciones periódicas.\n" +
                            "- No realizar modificaciones estructurales sin permiso.\n" +
                            "- Cumplir con los reglamentos internos del edificio.", normalFont));

            document.add(new Paragraph(" "));

            document.add(new Paragraph("CLÁUSULA SEXTA: TERMINACIÓN ANTICIPADA", subTitleFont));
            document.add(new Paragraph(
                    "Cualquiera de las partes podrá dar por terminado este contrato de forma anticipada notificando a la otra parte con un mínimo de 30 días de antelación, sin perjuicio de las penalidades pactadas en la cláusula adicional.", normalFont));

            document.add(new Paragraph(" "));

            document.add(new Paragraph("CLÁUSULA SÉPTIMA: JURISDICCIÓN", subTitleFont));
            document.add(new Paragraph(
                    "Para cualquier controversia que se derive del presente contrato, las partes se someten a la jurisdicción de los tribunales de la ciudad correspondiente al domicilio del ARRENDADOR.", normalFont));

            document.add(new Paragraph(" "));
            document.add(new Paragraph("El presente contrato se firma en señal de conformidad por ambas partes.", normalFont));

            document.add(new Paragraph(" "));

            document.add(new Paragraph("_____________________________", boldFont));
            document.add(new Paragraph("Firma del ARRENDADOR", normalFont));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("_____________________________", boldFont));
            document.add(new Paragraph("Firma del ARRENDATARIO", normalFont));

            document.close();
        }

        return tempFile.toFile();
    }

    public File generarBoletaPagoPdf(@NotNull Pago pago) throws Exception {
        Path tempFile = Files.createTempFile("boleta_pago_" + pago.getId(), ".pdf");

        try (FileOutputStream out = new FileOutputStream(tempFile.toFile())) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
            Font normalFont = new Font(Font.FontFamily.HELVETICA, 12);
            Font boldFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);

            Paragraph title = new Paragraph("BOLETA DE PAGO", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Fecha de emisión: " + LocalDate.now(), normalFont));
            document.add(new Paragraph("Periodo de pago: " + pago.getPeriodo(), normalFont));
            document.add(new Paragraph("Estado: " + pago.getEstado(), normalFont));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Datos del propietario:", boldFont));
            document.add(new Paragraph("Nombre: " + pago.getContrato().getPropietario().getNombres() + " " + pago.getContrato().getPropietario().getApellidos(), normalFont));
            document.add(new Paragraph("DNI: " + pago.getContrato().getPropietario().getDni(), normalFont));
            document.add(new Paragraph("Correo: " + pago.getContrato().getPropietario().getCorreo(), normalFont));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Departamento: " + pago.getContrato().getDepartamento().getNumero(), normalFont));
            document.add(new Paragraph("Monto: S/. " + pago.getMonto(), boldFont));

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Gracias por cumplir con sus obligaciones. Para cualquier duda o consulta, comunicarse con la administración del condominio.", normalFont));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Atentamente,", normalFont));
            document.add(new Paragraph("Administración del condominio", boldFont));

            document.close();
        }

        return tempFile.toFile();
    }*/

public File generarContratoPdf(@NotNull Contrato contrato) throws Exception {
    String filePath = "C:/Users/Admin/Documents/contrato_" + contrato.getId() + ".pdf";
    File file = new File(filePath);

    PdfWriter writer = new PdfWriter(new FileOutputStream(file));
    PdfDocument pdf = new PdfDocument(writer);
    Document document = new Document(pdf);

    try {
        // Definir fuentes para iText 7
        PdfFont titleFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont subTitleFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont normalFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

        // Título
        Paragraph title = new Paragraph("CONTRATO DE ARRENDAMIENTO DE INMUEBLE")
                .setFont(titleFont)
                .setFontSize(16)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(title);

        document.add(new Paragraph(" "));

        document.add(new Paragraph("Conste por el presente documento privado, el Contrato de Arrendamiento de Inmueble que celebran de una parte. La empresa en calidad de ARRENDADOR, representada por su administrador, y de la otra parte: "+contrato.getPropietario().getNombres() + " " + contrato.getPropietario().getApellidos()
                + ", identificado con DNI N° " + contrato.getPropietario().getDni()
                + ", con domicilio en el mismo edificio, quien actúa en calidad de ARRENDATARIO.")
                .setFont(normalFont)
                .setFontSize(12));


        document.add(new Paragraph(" "));

        document.add(new Paragraph("Las partes acuerdan lo siguiente:")
                .setFont(boldFont)
                .setFontSize(12));

        document.add(new Paragraph(" "));

        // CLÁUSULAS
        document.add(new Paragraph("CLÁUSULA PRIMERA: OBJETO DEL CONTRATO")
                .setFont(subTitleFont)
                .setFontSize(13));

        document.add(new Paragraph(
                "El ARRENDADOR da en arrendamiento al ARRENDATARIO el departamento N° " +
                        contrato.getDepartamento().getNumero() + ", ubicado en el piso " + contrato.getDepartamento().getPiso() +
                        " del edificio perteneciente a la empresa, con un área de " + contrato.getDepartamento().getArea() + " m².")
                .setFont(normalFont)
                .setFontSize(12));

        document.add(new Paragraph(" "));

        document.add(new Paragraph("CLÁUSULA SEGUNDA: PLAZO DEL CONTRATO")
                .setFont(subTitleFont)
                .setFontSize(13));

        document.add(new Paragraph(
                "El presente contrato tiene una duración de " + contrato.getCantidadMeses() + " meses, "
                        + "iniciando el " + contrato.getFechaInicio() + " y culminando el " + contrato.getFechaFin() + ".")
                .setFont(normalFont)
                .setFontSize(12));

        document.add(new Paragraph(" "));

        document.add(new Paragraph("CLÁUSULA TERCERA: MONTO DEL ARRENDAMIENTO")
                .setFont(subTitleFont)
                .setFontSize(13));

        document.add(new Paragraph(
                "El ARRENDATARIO pagará un monto mensual de S/ "
                        + (contrato.getTipo() == Contrato.TipoContrato.ALQUILER
                        ? contrato.getMontoTotal() / contrato.getCantidadMeses()
                        : contrato.getMontoTotal()) +
                        ". El pago deberá realizarse dentro de los primeros cinco (5) días de cada mes.")
                .setFont(normalFont)
                .setFontSize(12));

        document.add(new Paragraph(" "));

        document.add(new Paragraph("CLÁUSULA CUARTA: DESTINO DEL INMUEBLE")
                .setFont(subTitleFont)
                .setFontSize(13));

        document.add(new Paragraph(
                "El departamento será destinado exclusivamente a fines de vivienda. "
                        + "No podrá ser usado para fines comerciales ni subarrendado sin autorización escrita del ARRENDADOR.")
                .setFont(normalFont)
                .setFontSize(12));

        document.add(new Paragraph(" "));

        document.add(new Paragraph("CLÁUSULA QUINTA: OBLIGACIONES DEL ARRENDATARIO")
                .setFont(subTitleFont)
                .setFontSize(13));

        // Para las obligaciones, crear párrafos separados para mejor formato
        document.add(new Paragraph("• Conservar el inmueble en buen estado.")
                .setFont(normalFont)
                .setFontSize(12));
        document.add(new Paragraph("• Permitir inspecciones periódicas.")
                .setFont(normalFont)
                .setFontSize(12));
        document.add(new Paragraph("• No realizar modificaciones estructurales sin permiso.")
                .setFont(normalFont)
                .setFontSize(12));
        document.add(new Paragraph("• Cumplir con los reglamentos internos del edificio.")
                .setFont(normalFont)
                .setFontSize(12));

        document.add(new Paragraph(" "));

        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));

        document.add(new Paragraph("_____________________________"+"                         "+"_____________________________")
                .setFont(boldFont)
                .setFontSize(12));
        document.add(new Paragraph("  "+"         Firma del ARRENDADOR"+"                                  "+"         Firma del ARRENDATARIO")
                .setFont(normalFont)
                .setFontSize(12));

    } finally {
        document.close();
    }

    return file;
}

public File generarBoletaPagoPdf(@NotNull Pago pago) throws Exception {
    Path tempFile = Files.createTempFile("boleta_pago_" + pago.getId(), ".pdf");

    PdfWriter writer = new PdfWriter(Files.newOutputStream(tempFile));
    PdfDocument pdf = new PdfDocument(writer);
    Document document = new Document(pdf);

    try {
        // Definir fuentes para iText 7
        PdfFont titleFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont normalFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

        Paragraph title = new Paragraph("BOLETA DE PAGO")
                .setFont(titleFont)
                .setFontSize(16)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(title);

        document.add(new Paragraph(" "));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        document.add(new Paragraph("Fecha de emisión: " + LocalDate.now().format(formatter))
                .setFont(normalFont)
                .setFontSize(12));

        document.add(new Paragraph("Periodo de pago: " + pago.getPeriodo())
                .setFont(normalFont)
                .setFontSize(12));

        document.add(new Paragraph("Estado: " + pago.getEstado())
                .setFont(normalFont)
                .setFontSize(12));

        document.add(new Paragraph(" "));

        document.add(new Paragraph("Datos del propietario:")
                .setFont(boldFont)
                .setFontSize(12));

        document.add(new Paragraph("Nombre: " + pago.getContrato().getPropietario().getNombres() + " " + pago.getContrato().getPropietario().getApellidos())
                .setFont(normalFont)
                .setFontSize(12));

        document.add(new Paragraph("DNI: " + pago.getContrato().getPropietario().getDni())
                .setFont(normalFont)
                .setFontSize(12));

        document.add(new Paragraph("Correo: " + pago.getContrato().getPropietario().getCorreo())
                .setFont(normalFont)
                .setFontSize(12));

        document.add(new Paragraph(" "));

        document.add(new Paragraph("Departamento: " + pago.getContrato().getDepartamento().getNumero())
                .setFont(normalFont)
                .setFontSize(12));

        document.add(new Paragraph("Monto: S/. " + pago.getMonto())
                .setFont(boldFont)
                .setFontSize(14));

        document.add(new Paragraph(" "));
        document.add(new Paragraph("Gracias por cumplir con sus obligaciones. Para cualquier duda o consulta, comunicarse con la administración del condominio.")
                .setFont(normalFont)
                .setFontSize(12));

        document.add(new Paragraph(" "));

        document.add(new Paragraph("Atentamente,")
                .setFont(normalFont)
                .setFontSize(12));

        document.add(new Paragraph("Administración del condominio")
                .setFont(boldFont)
                .setFontSize(12));

    } finally {
        document.close();
    }

    return tempFile.toFile();
}
}
