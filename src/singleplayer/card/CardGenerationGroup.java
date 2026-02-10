package singleplayer.card;

import foundation.WeightedSelector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Function;

/**
 * Generates card attributes based on a step sequence.
 * <pre>
 *            +--------+              +--------+
 *            |        |              | step2A |
 * reset() -> | step1  | -> next() -> |        | -> ...
 *            |        |              | step2B |
 *            +--------+              +--------+
 * </pre>
 * Each box is a step group. When {@code next()} is called, an {@code AttributeGroup} is returned from the current step,
 * or, if the current step is finished, one of the steps in the next step group is selected based on a weight.
 * </br></br>
 * New steps are registered using {@code step()}. Once the step is fully defined, use {@code extend()} to add that
 * step to the end in a new group, or {@code alongside()} to add the step alongside others to the last step group
 * </br></br>
 * The example above would have been created as such:
 * <pre>
 *     {@code
 *     CardGenerationGroup g = ...
 *     g.step().extend() //create step1 in a new group
 *     g.step().extend() //create step2A in a new group
 *     g.step().alongside() //create step2B, and put it in the last existing group
 *     }
 * </pre>
 * It is between {@code step()} and {@code extend()}/{@code alongside()} that the contents of each step
 * should be decided, using {@code add()}:
 * <pre>
 *     {@code
 *     CardGenerationGroup g = ...
 *     //create step1 with two components
 *     g.step().add().add().extend()
 *
 *     //create step2 with one component
 *     g.step().add().alongside()
 *     }
 * </pre>
 */
public class CardGenerationGroup {
    public int count = 0, index = 0;
    private GenerationElement element;
    private final ArrayList<WeightedSelector<GenerationElement>> elements = new ArrayList<>();
    public final CardType cardType;

    public CardGenerationGroup(CardType cardType) {
        this.cardType = cardType;
    }

    /**
     * Add a new step in the selection process.
     * @param probability The probability that this step is executed, instead of being skipped
     * @param max The maximum number of times that this step is can be executed before skipping
     * @return A new {@code GenerationSelector} that specifies the newly created step
     */
    public GenerationSelector step(float probability, int max) {
        return new GenerationSelector(probability, max);
    }

    /**
     * Generate the next attribute group
     * @param handler
     * @return The group to be returned
     */
    public AttributeGroup next(AttributeHandler handler) {
        while (true) {
            System.out.println("next");
            System.out.println(index);
            System.out.println(count);
            System.out.println(elements);
            if (index >= elements.size()) {
                return null;
            }
            element = elements.get(index).get();
            if (Math.random() > element.probability || (count == element.max && count != -1)) {
                index++;
                count = 0;
                continue;
            }
            count++;
            return element.attributeGroups.get().apply(handler);
        }
    }

    /**
     * Resets the generator. Should always be done before use.
     */
    public void reset() {
        count = 0;
        index = 0;
        element = null;
    }

    private record GenerationElement(float probability, int max,
                                     WeightedSelector<Function<AttributeHandler, AttributeGroup>> attributeGroups) {

    }

    public class GenerationSelector {
        private final float probability;
        private final int max;
        private final WeightedSelector<Function<AttributeHandler, AttributeGroup>> selector = new WeightedSelector<>();

        public GenerationSelector(float probability, int max) {
            this.probability = probability;
            this.max = max;
        }

        /**
         * Add new possible {@code AttributeGroup} in the current step
         * @param weight
         * @param identifiers
         * @return
         */
        public GenerationSelector add(float weight, Object... identifiers) {
            selector.add(weight, h -> h.getGroup(identifiers));
            return this;
        }

        /**
         * Adds this step alongside the previous step in the same step group
         * @param weight The weight that this step has in the current step group
         * @return
         */
        public CardGenerationGroup alongside(float weight) {
            elements.getLast().add(weight, new GenerationElement(probability, max, selector));
            return CardGenerationGroup.this;
        }

        public CardGenerationGroup extend() {
            return extend(1);
        }

        /**
         * Appends this step to the end of the sequence, creating a new step group
         * @param weight The weight that this step has in the current step group
         * @return
         */
        public CardGenerationGroup extend(float weight) {
            WeightedSelector<GenerationElement> s = new WeightedSelector<>();
            s.add(weight, new GenerationElement(probability, max, selector));
            elements.add(s);
            return CardGenerationGroup.this;
        }
    }
}
