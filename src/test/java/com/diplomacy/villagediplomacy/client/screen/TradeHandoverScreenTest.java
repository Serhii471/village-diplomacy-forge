package com.diplomacy.villagediplomacy.client.screen;

import com.diplomacy.villagediplomacy.menu.TradeHandoverSnapshot;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TradeHandoverScreenTest {
    @Test
    void readyHintMatchesEveryContractPhase() {
        assertEquals("screen.villagediplomacy.trade_handover.next.ready.outbound_pickup",
                TradeHandoverScreen.nextStepKey(TradeHandoverSnapshot.Phase.OUTBOUND_PICKUP, true));
        assertEquals("screen.villagediplomacy.trade_handover.next.ready.outbound_delivery",
                TradeHandoverScreen.nextStepKey(TradeHandoverSnapshot.Phase.OUTBOUND_DELIVERY, true));
        assertEquals("screen.villagediplomacy.trade_handover.next.ready.return_pickup",
                TradeHandoverScreen.nextStepKey(TradeHandoverSnapshot.Phase.RETURN_PICKUP, true));
        assertEquals("screen.villagediplomacy.trade_handover.next.ready.return_delivery",
                TradeHandoverScreen.nextStepKey(TradeHandoverSnapshot.Phase.RETURN_DELIVERY, true));
        assertEquals("screen.villagediplomacy.trade_handover.next.ready.claim_reward",
                TradeHandoverScreen.nextStepKey(TradeHandoverSnapshot.Phase.CLAIM_REWARD, true));
    }

    @Test
    void waitingHintMatchesEveryContractPhase() {
        assertEquals("screen.villagediplomacy.trade_handover.next.wait.outbound_pickup",
                TradeHandoverScreen.nextStepKey(TradeHandoverSnapshot.Phase.OUTBOUND_PICKUP, false));
        assertEquals("screen.villagediplomacy.trade_handover.next.wait.outbound_delivery",
                TradeHandoverScreen.nextStepKey(TradeHandoverSnapshot.Phase.OUTBOUND_DELIVERY, false));
        assertEquals("screen.villagediplomacy.trade_handover.next.wait.return_pickup",
                TradeHandoverScreen.nextStepKey(TradeHandoverSnapshot.Phase.RETURN_PICKUP, false));
        assertEquals("screen.villagediplomacy.trade_handover.next.wait.return_delivery",
                TradeHandoverScreen.nextStepKey(TradeHandoverSnapshot.Phase.RETURN_DELIVERY, false));
        assertEquals("screen.villagediplomacy.trade_handover.next.wait.claim_reward",
                TradeHandoverScreen.nextStepKey(TradeHandoverSnapshot.Phase.CLAIM_REWARD, false));
    }
}
