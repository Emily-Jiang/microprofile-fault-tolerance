package org.eclipse.microprofile.fault.tolerance.tck.config;/*
 *******************************************************************************
 * Copyright (c) 2019 Contributors to the Eclipse Foundation
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.eclipse.microprofile.fault.tolerance.tck.util.TCKConfig;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.jboss.shrinkwrap.api.asset.Asset;

public class ConfigAnnotationAsset implements Asset {

    private final Properties props = new Properties();

    @Override
    public InputStream openStream() {
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            props.store(os, null);
            return new ByteArrayInputStream(os.toByteArray());
        }
        catch (IOException e) {
            // Shouldn't happen since we're only using in memory streams
            throw new RuntimeException("Unexpected error saving properties", e);
        }
    }
    
    /**
     * Generate config which scales the timeout values of all annotations on a single method
     * <p>
     * The following values are scaled using the scale factor in TCKConfig:
     * <ul>
     * <li>Retry.maxDuration</li>
     * <li>Retry.delay</li>
     * <li>Retry.jitter</li>
     * <li>Timeout.value</li>
     * <li>CircuitBreaker.delay</li>
     * </ul>
     */
    public ConfigAnnotationAsset autoscaleMethod(Class<?> clazz, final String method) {
        List<Method> methods = Arrays.stream(clazz.getMethods())
            .filter(m -> m.getName().equals(method))
            .collect(Collectors.toList());
        
        if (methods.size() == 0) {
            throw new RuntimeException("No such method " + method + " on class " + clazz.getName());
        }
        
        if (methods.size() > 1) {
            throw new RuntimeException("More than one method named " + method + " on class " + clazz.getName());
        }
        
        TCKConfig config = TCKConfig.getConfig();
        
        Method m = methods.get(0);
        Retry retry = m.getAnnotation(Retry.class);
        if (retry != null) {
            Duration maxDuration = Duration.of(retry.maxDuration(), retry.durationUnit());
            props.put(keyFor(clazz, method, Retry.class, "maxDuration"), config.getTimeoutInStr(maxDuration.toMillis()));
            props.put(keyFor(clazz, method, Retry.class, "maxDurationUnit"), ChronoUnit.MILLIS.name());
            
            Duration delay = Duration.of(retry.delay(), retry.delayUnit());
            props.put(keyFor(clazz, method, Retry.class, "delay"), config.getTimeoutInStr(delay.toMillis()));
            props.put(keyFor(clazz, method, Retry.class, "delayUnit"), ChronoUnit.MILLIS.name());
            
            Duration jitter = Duration.of(retry.jitter(), retry.jitterDelayUnit());
            props.put(keyFor(clazz, method, Retry.class, "jitter"), config.getTimeoutInStr(jitter.toMillis()));
            props.put(keyFor(clazz, method, Retry.class, "jitterDelayUnit"), ChronoUnit.MILLIS.name());
        }
        
        Timeout timeout = m.getAnnotation(Timeout.class);
        if (timeout != null) {
            Duration maxDuration = Duration.of(timeout.value(), timeout.unit());
            props.put(keyFor(clazz, method, Timeout.class, "value"), config.getTimeoutInStr(maxDuration.toMillis()));
            props.put(keyFor(clazz, method, Timeout.class, "unit"), ChronoUnit.MILLIS.name());
        }
        
        CircuitBreaker cb = m.getAnnotation(CircuitBreaker.class);
        if (cb != null) {
            Duration delay = Duration.of(cb.delay(), cb.delayUnit());
            props.put(keyFor(clazz, method, CircuitBreaker.class, "delay"), config.getTimeoutInStr(delay.toMillis()));
            props.put(keyFor(clazz, method, CircuitBreaker.class, "delayUnit"), ChronoUnit.MILLIS.name());
        }
        
        return this;
    }

    public ConfigAnnotationAsset setValue(final Class<?> clazz, final String method,
                                          final Class<? extends Annotation> annotation, final String value) {
        props.put(keyFor(clazz, method, annotation, "value"), value);
        return this;
    }

    /**
     * Build config key used to enable an annotation for a class and method
     * <p>
     * E.g. {@code com.example.MyClass/myMethod/Retry/enabled}
     *
     * @param clazz      may be null
     * @param method     may be null
     * @param annotation required
     * @return config key
     */
    private String keyFor(final Class<?> clazz, final String method, final Class<? extends Annotation> annotation,
                          final String property) {
        StringBuilder sb = new StringBuilder();
        if (clazz != null) {
            sb.append(clazz.getCanonicalName());
            sb.append("/");
        }

        if (method != null) {
            sb.append(method);
            sb.append("/");
        }

        sb.append(annotation.getSimpleName());
        sb.append("/");
        sb.append(property);

        return sb.toString();
    }

    public ConfigAnnotationAsset mergeProperties(final Properties properties) {
        this.props.putAll(properties);
        return this;
    }
}
