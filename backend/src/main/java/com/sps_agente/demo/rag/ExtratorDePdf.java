package com.sps_agente.demo.rag;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.nio.file.Path;


public class ExtratorDePdf {

    public static String extrair(Path caminho) throws IOException {
        try (PDDocument doc = Loader.loadPDF(caminho.toFile())) {
            var stripper = new PDFTextStripper();
            return stripper.getText(doc);
        }
    }
}
