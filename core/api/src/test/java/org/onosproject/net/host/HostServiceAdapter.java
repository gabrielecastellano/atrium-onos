/*
 * Copyright 2014 Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onosproject.net.host;

import org.onlab.packet.IpAddress;
import org.onlab.packet.MacAddress;
import org.onlab.packet.VlanId;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Host;
import org.onosproject.net.HostId;

import java.util.Set;

/**
 * Test adapter for host service.
 */
public class HostServiceAdapter implements HostService {
    @Override
    public int getHostCount() {
        return 0;
    }

    @Override
    public Iterable<Host> getHosts() {
        return null;
    }

    @Override
    public Host getHost(HostId hostId) {
        return null;
    }

    @Override
    public Set<Host> getHostsByVlan(VlanId vlanId) {
        return null;
    }

    @Override
    public Set<Host> getHostsByMac(MacAddress mac) {
        return null;
    }

    @Override
    public Set<Host> getHostsByIp(IpAddress ip) {
        return null;
    }

    @Override
    public Set<Host> getConnectedHosts(ConnectPoint connectPoint) {
        return null;
    }

    @Override
    public Set<Host> getConnectedHosts(DeviceId deviceId) {
        return null;
    }

    @Override
    public void startMonitoringIp(IpAddress ip) {
    }

    @Override
    public void stopMonitoringIp(IpAddress ip) {
    }

    @Override
    public void requestMac(IpAddress ip) {
    }

    @Override
    public void addListener(HostListener listener) {
    }

    @Override
    public void removeListener(HostListener listener) {
    }

}
