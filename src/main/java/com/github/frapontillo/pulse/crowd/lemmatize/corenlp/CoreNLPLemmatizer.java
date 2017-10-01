package com.github.frapontillo.pulse.crowd.lemmatize.corenlp;

import com.github.frapontillo.pulse.crowd.data.entity.Message;
import com.github.frapontillo.pulse.crowd.data.entity.Token;
import com.github.frapontillo.pulse.crowd.lemmatize.ILemmatizerOperator;
import com.github.frapontillo.pulse.spi.ISingleablePlugin;
import com.github.frapontillo.pulse.spi.VoidConfig;
import edu.stanford.nlp.process.Morphology;
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
public class CoreNLPLemmatizer extends ISingleablePlugin<Message, VoidConfig> {
    public final static String PLUGIN_NAME = "lemmatizer-stanford";
    private Morphology morphology;

    @Override public String getName() {
        return PLUGIN_NAME;
    }

    @Override public VoidConfig getNewParameter() {
        return new VoidConfig();
    }

    @Override public Observable.Operator<Message, Message> getOperator(VoidConfig parameters) {
        CoreNLPLemmatizer currentLemmatizer = this;
        return new ILemmatizerOperator(this) {
            @Override public List<Token> lemmatizeMessageTokens(Message message) {
                return currentLemmatizer.singleItemProcess(message).getTokens();
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
}
