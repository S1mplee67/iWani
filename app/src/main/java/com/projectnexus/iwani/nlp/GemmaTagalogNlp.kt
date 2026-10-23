package com.projectnexus.iwani.nlp

/**
 * Backwards compatibility delegate forwarding to the active Qwen2.5-0.5B-Instruct pipeline.
 */
object GemmaTagalogNlp {
    fun refineGlossOffline(glossTokens: List<String>, isTagalog: Boolean = false): String {
        return QwenFslTranslator.translate(glossTokens, isTagalog)
    }
}
