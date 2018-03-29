package org.hesperides.infrastructure.redis.eventstores;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * index les events
 */
@Slf4j
@Component
class EventsIndexer {

    private final DefaultRedisScript<Long> indexEventsScript;
    private final StringRedisTemplate template;

    EventsIndexer(StringRedisTemplate template) {
        this.template = template;
        indexEventsScript = new DefaultRedisScript<>();
        indexEventsScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("redis/index_events.lua")));
        indexEventsScript.setResultType(Long.class);
    }

    private List<String> retrieveAllKeys(StopWatch watch) {
        watch.start("retrieve all keys ");
        List<String> keys = template.keys("*").stream().filter(this::isAEventSourcedKey).collect(Collectors.toList());
        watch.stop();
        return keys;
    }

    private void restoreSaveConfiguration(String currentSave) {
        template.getConnectionFactory().getConnection().setConfig("save", currentSave);
    }

    private void disableRdb(StopWatch watch) {
        watch.start("Disable redis 'save' processing");
        template.getConnectionFactory().getConnection().setConfig("save", "");
        watch.stop();
    }

    private String getCurrentSaveConfiguration(StopWatch watch) {
        watch.start("Saving redis current 'save' configuration");
        List<String> saveList = template.getConnectionFactory().getConnection().getConfig("save");
        String currentSave = "";
        if (!saveList.isEmpty()) {
            currentSave = saveList.get(1);
        }
        log.info("Saving 'save' configuration : {}", currentSave);
        watch.stop();
        return currentSave;
    }

    private boolean isAEventSourcedKey(Object key) {
        return key.toString().startsWith("module-") || key.toString().startsWith("platform-") || key.toString().startsWith("template_package-");
    }

    void rebuildIfNecessary() {

//            if (template.hasKey(A_EVENTS_INDEX_LIST) && template.hasKey(A_EVENTS_INDEX_SET)) {
//                log.info("no need to rebuild index ...");
//                return;
//            }
        // rebuild l'index ici.
        StopWatch watch = new StopWatch();

        List<String> keys = retrieveAllKeys(watch);

        // desactivation du système de persistence, parce qu'on va ajouter beaucoup de clé !!
        String currentSave = getCurrentSaveConfiguration(watch);

        // desactive rdb
        disableRdb(watch);

        watch.start("indexing aggregates.");
        MultiThreadedIndexer indexer = new MultiThreadedIndexer();
        indexer.process(keys);
        watch.stop();

        // restore rdb
        restoreSaveConfiguration(currentSave);

        log.info("{}", watch.prettyPrint());
        log.info("Processed: keys: {}, noop: {}, ok: {}", indexer.keyCount, indexer.noopCount, indexer.okCount);
        log.info("done. total time: {} ms", watch.getTotalTimeMillis());
    }

    class MultiThreadedIndexer {

        AtomicInteger keyCount = new AtomicInteger();
        AtomicInteger okCount = new AtomicInteger();
        AtomicInteger koCount = new AtomicInteger();
        AtomicInteger noopCount = new AtomicInteger();

        void process(List<String> keys) {

            int processorsCount = 50;
            List<List<String>> batches = Lists.partition(keys, keys.size() / processorsCount);

            ExecutorService threadPool = Executors.newFixedThreadPool(batches.size() + 2);

            List<Callable<Object>> callables = batches.stream().map(batch -> Executors.callable(() -> doWithBatchOfKeys(batch))).collect(Collectors.toList());

            // thread de contrôle.
            ScheduledExecutorService controlService = Executors.newSingleThreadScheduledExecutor();
            controlService.scheduleAtFixedRate(() -> log.info("current key count {}, ok={}, ko={}, noop={}",
                    keyCount.get(), okCount.get(), koCount.get(), noopCount.get()), 1, 1, TimeUnit.SECONDS);

            try {
                threadPool.invokeAll(callables);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
                Thread.currentThread().interrupt();
            }
            controlService.shutdownNow();
        }

        void doWithBatchOfKeys(List<String> batch) {

            for (String key : batch) {
                keyCount.incrementAndGet();
                try {
                    long result = template.execute(indexEventsScript, Collections.singletonList(key));
                    if (result > 0) {
                        okCount.incrementAndGet();
                    } else {
                        noopCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    koCount.incrementAndGet();
                }
            }
        }
    }

}
