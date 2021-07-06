/*
 *******************************************************************************
 * Copyright (c) 2017-2020 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.eclipse.microprofile.fault.tolerance.tck.bulkhead.clientserver;

import org.eclipse.microprofile.fault.tolerance.tck.util.Barrier;
import org.eclipse.microprofile.faulttolerance.Bulkhead;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * A simple class level Semaphore @Bulkhead(10)
 * 
 * @author Gordon Hutchison
 * @author Andrew Rouse
 */
@ApplicationScoped
@Bulkhead(10)
public class Bulkhead10ClassSemaphoreBean {

    public void test(Barrier barrier) {
        barrier.await();
    }

};
