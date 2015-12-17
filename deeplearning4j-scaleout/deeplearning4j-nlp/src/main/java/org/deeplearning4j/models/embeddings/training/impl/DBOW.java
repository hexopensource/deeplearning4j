package org.deeplearning4j.models.embeddings.training.impl;

import lombok.NonNull;
import org.deeplearning4j.models.embeddings.WeightLookupTable;
import org.deeplearning4j.models.embeddings.loader.VectorsConfiguration;
import org.deeplearning4j.models.embeddings.training.SequenceLearningAlgorithm;
import org.deeplearning4j.models.sequencevectors.sequence.Sequence;
import org.deeplearning4j.models.sequencevectors.sequence.SequenceElement;
import org.deeplearning4j.models.word2vec.wordstore.VocabCache;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author raver119@gmail.com
 */
public class DBOW<T extends SequenceElement> implements SequenceLearningAlgorithm<T>{
    protected VocabCache<T> vocabCache;
    protected WeightLookupTable<T> lookupTable;
    protected VectorsConfiguration configuration;


    protected int window;
    protected boolean useAdaGrad;
    protected double negative;

    protected SkipGram<T> skipGram = new SkipGram<>();

    public DBOW() {

    }

    @Override
    public String getCodeName() {
        return "dbow";
    }

    @Override
    public void configure(@NonNull VocabCache<T> vocabCache, @NonNull WeightLookupTable<T> lookupTable, @NonNull VectorsConfiguration configuration) {
        this.window = configuration.getWindow();
        this.useAdaGrad = configuration.isUseAdaGrad();
        this.negative = configuration.getNegative();

        skipGram.configure(vocabCache, lookupTable, configuration);
    }

    @Override
    public void learnSequence(Sequence<T> sequence, AtomicLong nextRandom, double learningRate) {

    }

    protected void dbow(int i, Sequence<T> sequence, int b, AtomicLong nextRandom, double alpha) {

        final T word = sequence.getElements().get(i);
        List<T> sentence = sequence.getElements();

        // TODO: fix this, there should be option to have few labels per sequence
        List<T> labels = new ArrayList<>(); //(List<T>) sequence.getSequenceLabel();
        labels.add(sequence.getSequenceLabel());
        //    final VocabWord word = labels.get(0);

        if (sequence.getSequenceLabel() == null) throw new IllegalStateException("Label is NULL");

        if(word == null || sentence.isEmpty())
            return;

        //   log.info("Training word: " + word.getLabel() +  " against label: " + labels.get(0).getLabel());

        int end =  window * 2 + 1 - b;
        for(int a = b; a < end; a++) {
            if(a != window) {
                int c = i - window + a;
                if(c >= 0 && c < labels.size()) {
                    T lastWord = labels.get(c);
                    skipGram.iterateSample(word, lastWord,nextRandom,alpha);
                }
            }
        }
    }
}
