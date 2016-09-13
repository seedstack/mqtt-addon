/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.mqtt.internal;

/**
 * Supported RejectedExecutionHandler policies :
 * <ul>
 * <li></li>
 * <li>ABORT:  {@link java.util.concurrent.ThreadPoolExecutor.AbortPolicy} is call.</li>
 * <li>DISCARD:  {@link java.util.concurrent.ThreadPoolExecutor.DiscardPolicy} is call.</li>
 * <li>CALLER_RUNS: {@link java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy} is call.</li>
 * <li>DISCARD_OLDEST: {@link java.util.concurrent.ThreadPoolExecutor.DiscardOldestPolicy} is call</li>
 * </ul>
 */
public enum RejectedExecutionPolicy {

    ABORT,
    DISCARD,
    CALLER_RUNS,
    DISCARD_OLDEST;

}
