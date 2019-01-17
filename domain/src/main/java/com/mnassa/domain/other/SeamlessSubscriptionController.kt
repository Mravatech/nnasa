package com.mnassa.domain.other

/**
 * This controller manages the subscriptions like this:
 *
 * Each new [register] call pronounces previous observer his new child
 * and cancels him after he gets a response. This way you always have
 * fresh data.
 *
 * @author Artem Chepurnoy
 */
class SeamlessSubscriptionController {
    private var root: Subscription? = null

    /**
     * @author Artem Chepurnoy
     */
    private data class Subscription(
        /**
         * Function to cancel the subscription
         * externally.
         */
        private val cancel: () -> Unit,
        private var child: Subscription? = null
    ) {

        var isCanceled = false
            private set

        /**
         * Cancels subscription's
         * descendants.
         */
        fun cancelDescendants(): Boolean {
            child?.cancel()
            child = null
            return isCanceled
        }

        fun cancel() {
            this.cancel.invoke()
            this.cancelDescendants()

            // Mark the subscription as canceled, so
            // we know it has been cancelled.
            isCanceled = true
        }

    }

    /**
     * Registers new observable seamlessly.
     * ```
     * observable: (
     *         // call it when your real observer gets new data
     *         () -> Boolean
     *     ) ->
     *         // calling this should remove your
     *         // real observer.
     *         () -> Unit
     * ```
     *
     * @return registration cancellation
     */
    fun register(observable: (() -> Boolean) -> () -> Unit): () -> Unit {
        synchronized(this@SeamlessSubscriptionController) {
            lateinit var cancel: () -> Unit

            val subscription = Subscription({
                // Cancel the subscription
                // externally.
                cancel()
            }, root)
                .also {
                    root = it
                }

            cancel = observable.invoke(subscription::cancelDescendants)
            return {
                synchronized(this@SeamlessSubscriptionController) {
                    // Clean-up the root subscription if we
                    // cancel it.
                    if (root == subscription) {
                        root = null
                    }

                    subscription.cancel()
                }
            }
        }
    }
}
