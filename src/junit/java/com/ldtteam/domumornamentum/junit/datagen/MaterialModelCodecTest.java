package com.ldtteam.domumornamentum.junit.datagen;

import com.google.gson.JsonParser;
import com.ldtteam.domumornamentum.client.model.loader.MateriallyTexturedUnbakedModel;
import com.ldtteam.domumornamentum.client.model.item.MateriallyTexturedItemModel;
import com.mojang.math.Quadrant;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class MaterialModelCodecTest {
    @Test
    void rotatedOverlaySurvivesRoundTrip() {
        var codec = MateriallyTexturedUnbakedModel.CODEC.codec();
        var model = codec.parse(JsonOps.INSTANCE, JsonParser.parseString("""
                {"parent":"domum_ornamentum:block/stair/stairs_spec", "overlay":"domum_ornamentum:block/allbrick/dark_brick_stair", "x":180,"y":270}
                """)).getOrThrow();
        var copy = codec.parse(JsonOps.INSTANCE, codec.encodeStart(JsonOps.INSTANCE, model).getOrThrow()).getOrThrow();
        assertThat(copy.x()).isEqualTo(Quadrant.R180);
        assertThat(copy.y()).isEqualTo(Quadrant.R270);
        assertThat(copy.overlay()).isEqualTo(model.overlay());
        assertThat(copy.parentLocation()).isEqualTo(model.parentLocation());
    }

    @Test
    void materialItemRetainsOverlayDependency() {
        var codec = MateriallyTexturedItemModel.Unbaked.CODEC.codec();
        var model = codec.parse(JsonOps.INSTANCE, JsonParser.parseString("""
                {"model":"domum_ornamentum:block/allbrick/block_spec", "overlay":"domum_ornamentum:block/allbrick/dark_brick"}
                """)).getOrThrow();
        assertThat(codec.parse(JsonOps.INSTANCE, codec.encodeStart(JsonOps.INSTANCE, model).getOrThrow()).getOrThrow()).isEqualTo(model);
    }
}
