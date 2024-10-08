package net.minecraft.world.level.entity;

import java.util.UUID;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.world.phys.AABB;

public class LevelEntityGetterAdapter<T extends EntityAccess> implements LevelEntityGetter<T> {
    private final EntityLookup<T> visibleEntities;
    private final EntitySectionStorage<T> sectionStorage;

    public LevelEntityGetterAdapter(EntityLookup<T> pVisibleEntities, EntitySectionStorage<T> pSectionStorage) {
        this.visibleEntities = pVisibleEntities;
        this.sectionStorage = pSectionStorage;
    }

    @Nullable
    @Override
    public T get(int pId) {
        return this.visibleEntities.getEntity(pId);
    }

    @Nullable
    @Override
    public T get(UUID pUuid) {
        return this.visibleEntities.getEntity(pUuid);
    }

    @Override
    public Iterable<T> getAll() {
        return this.visibleEntities.getAllEntities();
    }

    @Override
    public <U extends T> void get(EntityTypeTest<T, U> pTest, AbortableIterationConsumer<U> pConsumer) {
        this.visibleEntities.getEntities(pTest, pConsumer);
    }

    @Override
    public void get(AABB pBoundingBox, Consumer<T> pConsumer) {
        this.sectionStorage.getEntities(pBoundingBox, AbortableIterationConsumer.forConsumer(pConsumer));
    }

    @Override
    public <U extends T> void get(EntityTypeTest<T, U> pTest, AABB pBounds, AbortableIterationConsumer<U> pConsumer) {
        this.sectionStorage.getEntities(pTest, pBounds, pConsumer);
    }
}