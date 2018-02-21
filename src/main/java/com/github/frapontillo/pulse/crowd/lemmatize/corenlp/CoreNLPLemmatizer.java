package com.github.frapontillo.pulse.crowd.lemmatize.corenlp;

import com.github.frapontillo.pulse.crowd.data.entity.Message;
import com.github.frapontillo.pulse.crowd.data.entity.Token;
import com.github.frapontillo.pulse.crowd.lemmatize.ILemmatizerOperator;
import com.github.frapontillo.pulse.spi.IPluginConfig;
import com.github.frapontillo.pulse.spi.ISingleablePlugin;
import com.github.frapontillo.pulse.spi.PluginConfigHelper;
import com.github.frapontillo.pulse.util.PulseLogger;
import com.google.gson.JsonElement;
import edu.stanford.nlp.process.Morphology;
import org.apache.logging.log4j.Logger;
import rx.Observable;

import java.util.List;

/**
 * A {@link ISingleablePlugin} implementation that delegates the lemmatization process to the
 * Stanford CoreNLP modules.
 * Stanford CoreNLP is only able to lemmatize some languages.
 * <p/>
 * {@link Message}s that are marked as stop words won't be processed by this plugin.
 *
 * @author Francesco Pontillo
 */
public class CoreNLPLemmatizer extends ISingleablePlugin<Message, CoreNLPLemmatizer.CoreNLPLemmatizerConfig> {
    public final static String PLUGIN_NAME = "lemmatizer-stanford";
    private Morphology morphology;

    private final static Logger logger = PulseLogger.getLogger(CoreNLPLemmatizer.class);

    @Override public String getName() {
        return PLUGIN_NAME;
    }

    @Override public CoreNLPLemmatizerConfig getNewParameter() {
        return new CoreNLPLemmatizerConfig();
    }

    @Override public Observable.Operator<Message, Message> getOperator(CoreNLPLemmatizerConfig parameters) {
        CoreNLPLemmatizer currentLemmatizer = this;
        return new ILemmatizerOperator(this) {
            @Override public List<Token> lemmatizeMessageTokens(Message message) {
                if (parameters != null) {
                    switch (parameters.getCalculate()) {
                        case CoreNLPLemmatizerConfig.ALL:
                            return currentLemmatizer.singleItemProcess(message).getTokens();
                        case CoreNLPLemmatizerConfig.NEW:
                            return currentLemmatizer.singleItemProcessWithFilter(message).getTokens();
                        default:
                            return currentLemmatizer.singleItemProcess(message).getTokens();
                    }
                } else {
                    return currentLemmatizer.singleItemProcess(message).getTokens();
                }
            }
        };
    }

    @Override public Message singleItemProcess(Message message) {
        if (message.getTokens() != null) {
            message.getTokens().forEach(token -> {
                if (!token.isStopWord()) {
                    String lemma = getMorphology().lemma(token.getText(), token.getPos());
                    token.setLemma(lemma);
                }
            });
        }
        return message;
    }

    public Message singleItemProcessWithFilter(Message message) {
        if (message.getTokens() != null) {
            message.getTokens().forEach(token -> {
                if (!token.isStopWord() && token.getLemma() == null) {
                    String lemma = getMorphology().lemma(token.getText(), token.getPos());
                    token.setLemma(lemma);
                } else {
                    logger.info("Token skipped (lemma already exist)");
                }
            });
        }
        return message;
    }

    /**
     * Get the existing or build a CoreNLP {@link Morphology} if none exists.
     *
     * @return The CoreNLP {@link Morphology} instance.
     */
    private Morphology getMorphology() {
        if (morphology == null) {
            morphology = new Morphology();
        }
        return morphology;
    }

    class CoreNLPLemmatizerConfig implements IPluginConfig<CoreNLPLemmatizerConfig> {

        /**
         * Lemmatize of all tokens coming from the stream.
         */
        public static final String ALL = "all";

        /**
         * Lemmatize the tokens with no lemma (property is null).
         */
        public static final String NEW = "new";


        /**
         * Accepted values: NEW, ALL
         */
        private String calculate;

        @Override
        public CoreNLPLemmatizerConfig buildFromJsonElement(JsonElement jsonElement) {
            return PluginConfigHelper.buildFromJson(jsonElement, CoreNLPLemmatizerConfig.class);
        }

        public String getCalculate() {
            return calculate;
        }

        public void setCalculate(String calculate) {
            this.calculate = calculate;
        }
    }
}
