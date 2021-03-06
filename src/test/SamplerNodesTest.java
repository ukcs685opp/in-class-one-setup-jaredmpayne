/*
 * Copyright (C) 2016 Michael
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package test;

import core.Message;
import core.SimScenario;
import routing.CommunityAndRankRouter;
import routing.MessageRouter;
import routing.SamplerRouter;
import routing.centrality.ExternalCentrality;
import routing.community.ExternalCommunityDetection;
import routing.mobility.ExternalMobility;
import static test.AbstractRouterTest.ts;

/**
 *
 */
public class SamplerNodesTest extends AbstractRouterTest {
    private SamplerRouter r0;
    private SamplerRouter r1;
    private SamplerRouter r2;
    private SamplerRouter r3;
    private SamplerRouter r4;
    private SamplerRouter r5;
    private SamplerRouter r6;
    private Message m1;
    private Message m2;

    @Override
    protected void setUp() throws Exception {
        ts.setNameSpace(null);
        ts.putSetting("Group1" + "." + SimScenario.GROUP_ID_S, "n");
        ts.putSetting("Group1" + "." + SimScenario.NROF_HOSTS_S, "0");
        ts.putSetting("Group1" + "." + SimScenario.NROF_INTERF_S, "0");
        ts.putSetting("Group1" + "." + SimScenario.MOVEMENT_MODEL_S, "StationaryMovement");
        ts.putSetting("Group1" + "." + SimScenario.ROUTER_S, "PassiveRouter");
        ts.putSetting("Group1" + "." + "nodeLocation", "0, 0");
        
        ts.putSetting(MessageRouter.B_SIZE_S, "" + BUFFER_SIZE);
        ts.putSetting(SamplerRouter.SAMPLER_NS + "." + SamplerRouter.CENTRALITY_ALG_S, "ExternalCentrality");
        ts.putSetting(SamplerRouter.SAMPLER_NS + "." + ExternalCentrality.CENTRALITY_FILE_S, "test_files/centrality");
        ts.putSetting(SamplerRouter.SAMPLER_NS + "." + CommunityAndRankRouter.COMMUNITY_ALG_S, "ExternalCommunityDetection");
        ts.putSetting(SamplerRouter.SAMPLER_NS + "." + ExternalCommunityDetection.COMMUNITY_FILE_S, "test_files/communities");
        ts.putSetting(SamplerRouter.SAMPLER_NS + "." + SamplerRouter.MOBILITY_ALG_S, "ExternalMobility");
        ts.putSetting(SamplerRouter.SAMPLER_NS + "." + ExternalMobility.MOBILITY_FILE_S, "test_files/mobility");
        ts.putSetting(SamplerRouter.SAMPLER_NS + "." + SamplerRouter.RELAY_POINTS_S, "10-15");

        SamplerRouter router = new SamplerRouter(ts);
        setRouterProto(router);
        super.setUp();
        SimScenario.getInstance().getHosts().add(h0);
        SimScenario.getInstance().getHosts().add(h1);
        SimScenario.getInstance().getHosts().add(h2);
        SimScenario.getInstance().getHosts().add(h3);
        SimScenario.getInstance().getHosts().add(h4);
        SimScenario.getInstance().getHosts().add(h5);
        SimScenario.getInstance().getHosts().add(h6);
        m1 = new Message(h0, h6, msgId1, 1);
        m2 = new Message(h3, h6, msgId2, 1);
        r0 = (SamplerRouter) h0.getRouter();
        r0.setTestHost(h0);
        r1 = (SamplerRouter) h1.getRouter();
        r1.setTestHost(h1);
        r2 = (SamplerRouter) h2.getRouter();
        r2.setTestHost(h2);
        r3 = (SamplerRouter) h3.getRouter();
        r3.setTestHost(h3);
        r4 = (SamplerRouter) h4.getRouter();
        r4.setTestHost(h4);
        r5 = (SamplerRouter) h5.getRouter();
        r5.setTestHost(h5);
        r6 = (SamplerRouter) h6.getRouter();
        r6.setTestHost(h6);
    }

    private void advanceWorld(int seconds) {
        clock.advance(1);
        updateAllNodes();
    }

    public void testDirectDelivery() {
        // Create the message
        h0.createNewMessage(m1);
        checkCreates(1);
        updateAllNodes();

        // h0 -> h6
        h0.forceConnection(h6, h0.getInterfaces().get(0).getInterfaceType(), true);
        advanceWorld(1);
        assertTrue(mc.next());
        assertEquals(mc.getLastType(), mc.TYPE_START);
        assertEquals(mc.getLastFrom(), h0);
        assertEquals(mc.getLastTo(), h6);
        advanceWorld(1);
        assertTrue(mc.next());
        assertEquals(mc.getLastType(), mc.TYPE_RELAY);
        assertEquals(mc.getLastFrom(), h0);
        assertEquals(mc.getLastTo(), h6);
        assertTrue(mc.getLastFirstDelivery());
        h0.forceConnection(h6, h0.getInterfaces().get(0).getInterfaceType(), false);
        advanceWorld(1);
        //assertFalse(mc.next());
    }
    
    public void testToTargetCommunity() {
        // Create the message
        h0.createNewMessage(m1);
        checkCreates(1);
        updateAllNodes();
        
        // h0 -> h5
        h0.forceConnection(h5, h0.getInterfaces().get(0).getInterfaceType(), true);
        advanceWorld(1);
        assertTrue(mc.next());
        assertEquals(mc.getLastType(), mc.TYPE_START);
        assertEquals(mc.getLastFrom(), h0);
        assertEquals(mc.getLastTo(), h5);
        advanceWorld(1);
        assertTrue(mc.next());
        assertEquals(mc.getLastType(), mc.TYPE_RELAY);
        assertEquals(mc.getLastFrom(), h0);
        assertEquals(mc.getLastTo(), h5);
        assertFalse(mc.getLastFirstDelivery());
        h0.forceConnection(h5, h0.getInterfaces().get(0).getInterfaceType(), false);
        advanceWorld(1);
        assertFalse(mc.next());
    }
    
    public void testToGreaterLocalRank() {
        //Create the message
        h3.createNewMessage(m2);
        checkCreates(1);
        updateAllNodes();
        
        // h3 -> h4
        h3.forceConnection(h4, h3.getInterfaces().get(0).getInterfaceType(), true);
        advanceWorld(1);
        assertTrue(mc.next());
        assertEquals(mc.getLastType(), mc.TYPE_START);
        assertEquals(mc.getLastFrom(), h3);
        assertEquals(mc.getLastTo(), h4);
        advanceWorld(1);
        assertTrue(mc.next());
        assertEquals(mc.getLastType(), mc.TYPE_RELAY);
        assertEquals(mc.getLastFrom(), h3);
        assertEquals(mc.getLastTo(), h4);
        assertFalse(mc.getLastFirstDelivery());
        h3.forceConnection(h4, h3.getInterfaces().get(0).getInterfaceType(), false);
        advanceWorld(1);
        assertFalse(mc.next());
        
    }
    
    public void testToLesserLocalRank() {
        //Create the message
        h3.createNewMessage(m2);
        checkCreates(1);
        updateAllNodes();
        
        // h3 -> h5
        h3.forceConnection(h5, h3.getInterfaces().get(0).getInterfaceType(), true);
        advanceWorld(1);
        assertFalse(mc.next());
    }
    
    public void testToGreaterGlobalRank() {
        // Create the message
        h0.createNewMessage(m1);
        checkCreates(1);
        updateAllNodes();
        
        // h0 -> h1
        h0.forceConnection(h1, h0.getInterfaces().get(0).getInterfaceType(), true);
        advanceWorld(1);
        assertTrue(mc.next());
        assertEquals(mc.getLastType(), mc.TYPE_START);
        assertEquals(mc.getLastFrom(), h0);
        assertEquals(mc.getLastTo(), h1);
        advanceWorld(1);
        assertTrue(mc.next());
        assertEquals(mc.getLastType(), mc.TYPE_RELAY);
        assertEquals(mc.getLastFrom(), h0);
        assertEquals(mc.getLastTo(), h1);
        assertFalse(mc.getLastFirstDelivery());
        h3.forceConnection(h0, h1.getInterfaces().get(0).getInterfaceType(), false);
        advanceWorld(1);
        assertFalse(mc.next());
    }
    
    public void testToLesserGlobalRank() {
        // Create the message
        h0.createNewMessage(m1);
        checkCreates(1);
        updateAllNodes();
        
        // h0 -> h2
        h0.forceConnection(h2, h0.getInterfaces().get(0).getInterfaceType(), true);
        advanceWorld(1);
        assertFalse(mc.next());
    }
    
    public void testToOutsideTargetCommunity() {
        //Create the message
        h3.createNewMessage(m2);
        checkCreates(1);
        updateAllNodes();
        
        // h3 -> h4
        h3.forceConnection(h0, h3.getInterfaces().get(0).getInterfaceType(), true);
        advanceWorld(1);
        assertFalse(mc.next());
    }
}
