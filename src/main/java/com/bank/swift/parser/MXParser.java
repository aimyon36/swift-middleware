package com.bank.swift.parser;

import com.bank.swift.model.entity.ParsedBusinessData;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * MX (ISO 20022) 报文解析器
 * 使用标准 DOM 解析，后续可集成 Prowide
 */
@Slf4j
@Component
public class MXParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 解析MX报文
     */
    public ParsedBusinessData parse(String rawXml, String messageType) {
        log.info("解析MX报文: type={}", messageType);

        ParsedBusinessData data = new ParsedBusinessData();
        data.setBusinessType(determineBusinessType(messageType));

        try {
            // 使用 DOM 解析 MX XML
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(rawXml.getBytes(StandardCharsets.UTF_8)));

            // 存储原始 XML 便于调试
            Map<String, Object> rawData = new HashMap<>();
            rawData.put("xml", rawXml);
            rawData.put("messageType", messageType);

            // 根据报文类型解析
            switch (messageType) {
                case "pacs.008.001.08" -> parsePacs008(doc, data, rawData);
                case "pain.001.001.09" -> parsePain001(doc, data, rawData);
                case "pacs.002.001.08" -> parsePacs002(doc, data, rawData);
                case "camt.056.001.08" -> parseCamt056(doc, data, rawData);
                default -> parseGeneric(doc, data, rawData);
            }

            data.setRawJson(objectMapper.writeValueAsString(rawData));

        } catch (Exception e) {
            log.error("解析MX报文失败: type={}, error={}", messageType, e.getMessage());
            data.setRawJson("{\"parseError\":\"" + e.getMessage() + "\"}");
        }

        return data;
    }

    private void parsePacs008(Document doc, ParsedBusinessData data, Map<String, Object> rawData) throws Exception {
        data.setBusinessType("CREDIT_TRANSFER");

        // 尝试提取金额
        NodeList amountNodes = doc.getElementsByTagName("InstdAmt");
        if (amountNodes.getLength() > 0) {
            String amountStr = amountNodes.item(0).getTextContent();
            data.setAmount(new BigDecimal(amountStr));
            rawData.put("amount", amountStr);
        }

        // 提取币种
        NodeList ccyNodes = doc.getElementsByTagName("Ccy");
        if (ccyNodes.getLength() > 0) {
            String ccy = ccyNodes.item(0).getTextContent();
            data.setCurrency(ccy);
            rawData.put("currency", ccy);
        }

        // 提取参考号
        NodeList refNodes = doc.getElementsByTagName("EndToEndId");
        if (refNodes.getLength() > 0) {
            String ref = refNodes.item(0).getTextContent();
            data.setReference(ref);
            rawData.put("reference", ref);
        }

        // 提取源账户
        NodeList dbtrAcctNodes = doc.getElementsByTagName("DbtrAcct");
        if (dbtrAcctNodes.getLength() > 0) {
            Element acct = (Element) dbtrAcctNodes.item(0);
            NodeList idNodes = acct.getElementsByTagName("Id");
            if (idNodes.getLength() > 0) {
                data.setSourceAccount(idNodes.item(0).getTextContent());
                rawData.put("sourceAccount", idNodes.item(0).getTextContent());
            }
        }

        // 提取目标账户
        NodeList cdtrAcctNodes = doc.getElementsByTagName("CdtrAcct");
        if (cdtrAcctNodes.getLength() > 0) {
            Element acct = (Element) cdtrAcctNodes.item(0);
            NodeList idNodes = acct.getElementsByTagName("Id");
            if (idNodes.getLength() > 0) {
                data.setDestAccount(idNodes.item(0).getTextContent());
                rawData.put("destAccount", idNodes.item(0).getTextContent());
            }
        }

        data.setValueDate(LocalDate.now());
        log.info("解析 pacs.008 完成");
    }

    private void parsePain001(Document doc, ParsedBusinessData data, Map<String, Object> rawData) throws Exception {
        data.setBusinessType("PAYMENT_REQUEST");
        data.setCurrency("CNY");
        data.setValueDate(LocalDate.now());

        // 提取参考号
        NodeList refNodes = doc.getElementsByTagName("PmtInfId");
        if (refNodes.getLength() > 0) {
            String ref = refNodes.item(0).getTextContent();
            data.setReference(ref);
            rawData.put("paymentInfoId", ref);
        }

        log.info("解析 pain.001 完成");
    }

    private void parsePacs002(Document doc, ParsedBusinessData data, Map<String, Object> rawData) throws Exception {
        data.setBusinessType("PAYMENT_STATUS");
        data.setCurrency("CNY");

        NodeList orgnlEndToEndIdNodes = doc.getElementsByTagName("OrgnlEndToEndId");
        if (orgnlEndToEndIdNodes.getLength() > 0) {
            data.setReference(orgnlEndToEndIdNodes.item(0).getTextContent());
            rawData.put("originalEndToEndId", orgnlEndToEndIdNodes.item(0).getTextContent());
        }

        log.info("解析 pacs.002 完成");
    }

    private void parseCamt056(Document doc, ParsedBusinessData data, Map<String, Object> rawData) throws Exception {
        data.setBusinessType("PAYMENT_REVOKE");
        data.setCurrency("CNY");

        NodeList refNodes = doc.getElementsByTagName("OrgnlEndToEndId");
        if (refNodes.getLength() > 0) {
            data.setReference(refNodes.item(0).getTextContent());
            rawData.put("originalEndToEndId", refNodes.item(0).getTextContent());
        }

        log.info("解析 camt.056 完成");
    }

    private void parseGeneric(Document doc, ParsedBusinessData data, Map<String, Object> rawData) {
        data.setBusinessType("UNKNOWN");
        rawData.put("note", "使用通用解析");
        log.info("通用解析完成");
    }

    /**
     * 根据报文类型确定业务类型
     */
    private String determineBusinessType(String messageType) {
        if (messageType == null) {
            return "UNKNOWN";
        }
        if (messageType.startsWith("pacs.008")) {
            return "CREDIT_TRANSFER";
        } else if (messageType.startsWith("pain.001")) {
            return "PAYMENT_REQUEST";
        } else if (messageType.startsWith("pacs.002")) {
            return "PAYMENT_STATUS";
        } else if (messageType.startsWith("camt.056")) {
            return "PAYMENT_REVOKE";
        }
        return "UNKNOWN";
    }
}
