// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.util.indexing.roots;

import com.intellij.openapi.project.Project;
import com.intellij.util.Function;
import com.intellij.util.indexing.roots.IndexableEntityProvider.ParentEntityDependent;
import com.intellij.util.indexing.roots.builders.IndexableIteratorBuilders;
import com.intellij.workspaceModel.storage.WorkspaceEntity;
import com.intellij.workspaceModel.storage.bridgeEntities.api.JavaSourceRootEntity;
import com.intellij.workspaceModel.storage.bridgeEntities.api.ModuleEntity;
import com.intellij.workspaceModel.storage.bridgeEntities.api.SourceRootEntity;
import com.intellij.workspaceModel.storage.url.VirtualFileUrl;
import kotlin.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

class JavaSourceRootIndexableEntityProvider implements ParentEntityDependent<JavaSourceRootEntity, SourceRootEntity> {

  @Override
  public @NotNull Class<JavaSourceRootEntity> getEntityClass() {
    return JavaSourceRootEntity.class;
  }

  @Override
  public @NotNull Collection<? extends IndexableIteratorBuilder> getAddedEntityIteratorBuilders(@NotNull JavaSourceRootEntity entity,
                                                                                                @NotNull Project project) {
    return collectBuildersOnAddedEntityWithDataExtractor(entity, JavaSourceRootIndexableEntityProvider::getDataForBuilders);
  }

  @Override
  public @NotNull Collection<? extends IndexableIteratorBuilder> getReplacedEntityIteratorBuilders(@NotNull JavaSourceRootEntity oldEntity,
                                                                                                   @NotNull JavaSourceRootEntity newEntity) {
    return collectBuildersOnReplacedEntityWithDataExtractor(oldEntity, newEntity,
                                                            JavaSourceRootIndexableEntityProvider::getDataForBuilders);
  }

  static <E extends WorkspaceEntity> Collection<IndexableIteratorBuilder> collectBuildersOnAddedEntityWithDataExtractor(@NotNull E entity,
                                                                                                                        @NotNull Function<? super E, @NotNull Pair<VirtualFileUrl, ModuleEntity>> extractor) {
    Pair<VirtualFileUrl, ModuleEntity> data = extractor.fun(entity);
    return IndexableIteratorBuilders.INSTANCE.forModuleRoots(data.getSecond().getPersistentId(), data.getFirst());
  }

  @NotNull
  static <E extends WorkspaceEntity> Collection<? extends IndexableIteratorBuilder> collectBuildersOnReplacedEntityWithDataExtractor(
    @NotNull E oldEntity,
    @NotNull E newEntity,
    @NotNull Function<? super E, Pair<VirtualFileUrl, ModuleEntity>> extractor) {
    Pair<VirtualFileUrl, ModuleEntity> newData = extractor.fun(newEntity);
    if (newData != null) {
      Pair<VirtualFileUrl, ModuleEntity> oldData = extractor.fun(oldEntity);
      if (oldData == null || !newData.getFirst().equals(oldData.getFirst())) {
        return IndexableIteratorBuilders.INSTANCE.forModuleRoots(newData.getSecond().getPersistentId(), newData.getFirst());
      }
    }
    return Collections.emptyList();
  }

  private static @NotNull Pair<VirtualFileUrl, ModuleEntity> getDataForBuilders(@NotNull JavaSourceRootEntity entity) {
    SourceRootEntity sourceRootEntity = entity.getSourceRoot();
    return new Pair<>(sourceRootEntity.getUrl(), sourceRootEntity.getContentRoot().getModule());
  }

  @Override
  public @NotNull Class<SourceRootEntity> getParentEntityClass() {
    return SourceRootEntity.class;
  }

  @Override
  public @NotNull Collection<? extends IndexableIteratorBuilder> getReplacedParentEntityIteratorBuilder(@NotNull SourceRootEntity oldEntity,
                                                                                                        @NotNull SourceRootEntity newEntity,
                                                                                                        @NotNull Project project) {
    if(oldEntity.getJavaSourceRoots().equals(newEntity.getJavaSourceRoots())) return Collections.emptyList();
    return IndexableIteratorBuilders.INSTANCE.forModuleRoots(newEntity.getContentRoot().getModule().getPersistentId(), newEntity.getUrl());
  }
}
