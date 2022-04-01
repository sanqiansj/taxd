package com.niaobulashi.service;

import net.sf.json.JSONObject;
import org.springframework.stereotype.Service;

/**
 * @author y
 * @date 2022/3/29
 */
@Service
public interface PdfService {

    JSONObject analysispdf(String path);


    JSONObject analysisWord(String path);
}
