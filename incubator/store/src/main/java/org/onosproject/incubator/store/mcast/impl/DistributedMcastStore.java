package org.onosproject.incubator.store.mcast.impl;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onlab.packet.IpAddress;
import org.onlab.util.KryoNamespace;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.mcast.McastEvent;
import org.onosproject.net.mcast.McastRoute;
import org.onosproject.net.mcast.McastRouteInfo;
import org.onosproject.net.mcast.McastStore;
import org.onosproject.net.mcast.McastStoreDelegate;
import org.onosproject.store.AbstractStore;
import org.onosproject.store.service.ConsistentMap;
import org.onosproject.store.service.Serializer;
import org.onosproject.store.service.StorageService;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * A distributed mcast store implementation. Routes are stored consistently
 * across the cluster.
 */
@Component(immediate = true)
@Service
public class DistributedMcastStore extends AbstractStore<McastEvent, McastStoreDelegate>
        implements McastStore {
    //FIXME the number of events that will potentially be generated here is
    // not sustainable, consider changing this to an eventually consistent
    // map and not emitting events but rather use a provider-like mechanism
    // to program the dataplane.

    private static final String MCASTRIB = "mcast-rib-table";
    private Logger log = getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private StorageService storageService;

    protected ConsistentMap<McastRoute, MulticastData> mcastRib;
    protected Map<McastRoute, MulticastData> mcastRoutes;


    @Activate
    public void activate() {

        mcastRib = storageService.<McastRoute, MulticastData>consistentMapBuilder()
                .withName(MCASTRIB)
                .withSerializer(Serializer.using(KryoNamespace.newBuilder().register(
                        MulticastData.class,
                        McastRoute.class,
                        McastRoute.Type.class,
                        IpAddress.class,
                        List.class,
                        ConnectPoint.class
                ).build()))
                .withRelaxedReadConsistency()
                .build();

        mcastRoutes = mcastRib.asJavaMap();


        log.info("Started");
    }

    @Deactivate
    public void deactivate() {
        log.info("Stopped");
    }

    @Override
    public void storeRoute(McastRoute route, Type operation) {
        switch (operation) {
            case ADD:
                if (mcastRoutes.putIfAbsent(route, MulticastData.empty()) == null) {
                    delegate.notify(new McastEvent(McastEvent.Type.ROUTE_ADDED,
                                                   McastRouteInfo.mcastRouteInfo(route)));
                }
                break;
            case REMOVE:
                if (mcastRoutes.remove(route) != null) {
                    delegate.notify(new McastEvent(McastEvent.Type.ROUTE_REMOVED,
                                                   McastRouteInfo.mcastRouteInfo(route)));
                }
                break;
            default:
                log.warn("Unknown mcast operation type: {}", operation);
        }
    }

    @Override
    public void storeSource(McastRoute route, ConnectPoint source) {
        MulticastData data = mcastRoutes.compute(route, (k, v) -> {
            if (v == null) {
                return new MulticastData(source);
            } else {
                v.setSource(source);
            }
            return v;
        });


        if (data != null) {
            delegate.notify(new McastEvent(McastEvent.Type.SOURCE_ADDED,
                                           McastRouteInfo.mcastRouteInfo(route,
                                                                         data.sinks(),
                                                                         source)));
        }

    }

    @Override
    public void storeSink(McastRoute route, ConnectPoint sink, Type operation) {
        MulticastData data = mcastRoutes.compute(route, (k, v) -> {
            switch (operation) {
                case ADD:
                    if (v == null) {
                        v = MulticastData.empty();
                    }
                    v.appendSink(sink);
                    break;
                case REMOVE:
                    if (v != null) {
                        v.removeSink(sink);
                    }
                    break;
                default:
                    log.warn("Unknown mcast operation type: {}", operation);
            }
            return v;
        });


        if (data != null) {
            switch (operation) {
                case ADD:
                    delegate.notify(new McastEvent(
                            McastEvent.Type.SINK_ADDED,
                            McastRouteInfo.mcastRouteInfo(route,
                                                          sink,
                                                          data.source())));
                    break;
                case REMOVE:
                    if (data != null) {
                        delegate.notify(new McastEvent(
                                McastEvent.Type.SINK_REMOVED,
                                McastRouteInfo.mcastRouteInfo(route,
                                                              sink,
                                                              data.source())));
                    }
                    break;
                default:
                    log.warn("Unknown mcast operation type: {}", operation);
            }
        }

    }

    @Override
    public ConnectPoint sourceFor(McastRoute route) {
        return mcastRoutes.getOrDefault(route, MulticastData.empty()).source();
    }

    @Override
    public Set<ConnectPoint> sinksFor(McastRoute route) {
        return mcastRoutes.getOrDefault(route, MulticastData.empty()).sinks();
    }

}
