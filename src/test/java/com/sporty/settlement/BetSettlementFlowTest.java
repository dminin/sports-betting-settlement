package com.sporty.settlement;

import org.springframework.test.context.ActiveProfiles;

/**
 * Full flow with RocketMQ mocked in-process ({@code mock} profile).
 */
@ActiveProfiles("mock")
class BetSettlementFlowTest extends AbstractBetSettlementFlowTest {
}
