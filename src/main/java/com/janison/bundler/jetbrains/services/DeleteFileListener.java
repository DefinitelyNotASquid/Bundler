package com.janison.bundler.jetbrains.services;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.janison.bundler.jetbrains.infocollectors.SystemMetrics;
import com.janison.bundler.jetbrains.insightstelemetry.AzureTelemtryClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DeleteFileListener implements BulkFileListener {

    @Override
    public void after(List<? extends VFileEvent> events) {

        final boolean found =
                events.stream()
                        .anyMatch(VFileDeleteEvent.class::isInstance);


        if (!found) return;

        List<VirtualFile> VFiles =
                events.stream()
                .filter(VFileDeleteEvent.class::isInstance)
                .map(VFileEvent::getFile)
                .collect(Collectors.toList());

        AzureTelemtryClient at = new AzureTelemtryClient();

        SystemMetrics sm = new SystemMetrics();

        for (VirtualFile vf:VFiles) {

            Map<String, String> tempMap = new HashMap<>();

            tempMap.put("name", vf.getName());

            at.telemetryClient.trackEvent("File:Delete", sm.getEnvProperties(tempMap), sm.getMetrics());
        }

    }
}