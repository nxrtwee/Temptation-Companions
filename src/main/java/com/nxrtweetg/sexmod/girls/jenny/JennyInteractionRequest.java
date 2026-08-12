package com.nxrtweetg.sexmod.girls.jenny;

import com.nxrtweetg.sexmod.girls.scene.GirlSceneAction;
import java.util.UUID;

public record JennyInteractionRequest(int entityId, UUID playerId, GirlSceneAction action) {
}
