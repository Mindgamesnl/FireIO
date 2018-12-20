package io.fire.core.tests;

import io.fire.core.common.body.RequestString;
import io.fire.core.common.eventmanager.enums.Event;
import io.fire.core.common.eventmanager.enums.EventPriority;
import io.fire.core.common.io.http.enums.HttpContentType;
import io.fire.core.common.packets.PingPacket;
import io.fire.core.server.FireIoServer;
import io.fire.core.server.modules.balancingmodule.objects.BalancerConfiguration;
import io.fire.core.server.modules.client.superclasses.Client;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class TestServer {

    public static void main(String[] args) {

        FireIoServer server = null;
        try {
            server = new FireIoServer(80)
                    .setPassword("testpassword1")
                    .setThreadPoolSize(4)
                    .setRateLimiter(100000, 1)

                    .on(Event.CONNECT, client -> {
                        for(int i = 0; i < 50; ++i) {
                            System.out.println("Sending packet " + i);
                            client.send("channel", "i am message " + i);
                        }
                        System.out.println("A user connected via " + client.getConnectionType());
                    })

.on(Event.TIMED_OUT, client -> {
    System.out.println(client.getId() + " closed unexpectedly! " + client.getConnectionType());
})

                    .on(Event.DISCONNECT, eventPayload -> {
                        Client client = (Client) eventPayload;
                        System.out.println(client.getId() + " just disconnected");
                    });

                server.on("channel", (client, message) -> {
                    System.out.println("Channel got: " + message);
                });

        } catch (IOException e) {
            e.printStackTrace();
        }

        server.onRequest("whoami", (client, request, response) -> {
            System.out.println(client.getId().toString() + " asked who it is! sending ip back");
            response.complete(new RequestString("You are: " + client.getInfo().getHostname()));
        });


        Timer  timer = new Timer();

        server.onRequest("lorem", (client, request, response) -> {
            System.out.println(client.getId().toString() + " requested lorem");
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    response.complete(new RequestString("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aenean in diam ligula. Phasellus eleifend efficitur tortor vulputate maximus. Aliquam fringilla sem at ornare efficitur. Sed id nisi non tellus ultricies ultrices. Morbi hendrerit magna quis ante egestas hendrerit. Nulla luctus viverra ultricies. Vivamus libero neque, eleifend sed condimentum non, pretium id nunc. Sed porta pellentesque ipsum, quis convallis tellus suscipit sit amet. Phasellus in luctus leo. Etiam rhoncus odio urna, vel volutpat elit dapibus at.\n" +
                            "\n" +
                            "Nam iaculis, erat sed luctus facilisis, diam tellus porta urna, id convallis orci ipsum quis libero. Cras finibus porta fringilla. Nunc et efficitur urna. Nulla nulla magna, malesuada eget erat vel, tempor consequat ipsum. Suspendisse nec mollis leo. Duis elit velit, tempor ut egestas et, sagittis at odio. Duis iaculis, ex nec convallis rhoncus, nunc mi finibus magna, ut scelerisque lorem ex cursus tortor. Praesent vel velit turpis. Nunc ac nisi dignissim, hendrerit metus vehicula, venenatis elit. Vivamus efficitur odio eu malesuada ornare. Suspendisse ligula lacus, rutrum id finibus eu, pulvinar vel turpis. Curabitur tellus quam, malesuada nec nisl et, pulvinar tempor lacus. Proin eget lorem iaculis nisi venenatis condimentum. Aliquam erat volutpat. Curabitur fringilla, lorem in rhoncus ornare, magna elit elementum lorem, non rutrum ex lectus quis quam.\n" +
                            "\n" +
                            "Nam molestie, mi non cursus egestas, neque ex mattis nunc, in lobortis ex arcu at libero. Curabitur hendrerit, leo at ultrices luctus, sapien turpis interdum velit, nec pharetra metus elit et velit. Praesent eget ullamcorper augue. Mauris commodo, lorem at efficitur scelerisque, libero sapien consectetur mi, eget tempor metus nisi egestas sapien. Nulla aliquet felis leo, vitae porta diam vulputate sed. Curabitur finibus tellus eget vulputate pretium. Mauris porttitor venenatis faucibus. In tincidunt tellus id dictum scelerisque. Nulla gravida pharetra condimentum. Phasellus eros velit, lacinia in imperdiet et, porttitor sit amet urna. Interdum et malesuada fames ac ante ipsum primis in faucibus. Nulla ac viverra nulla. Nunc lobortis eros nec lectus convallis, id iaculis metus tristique. Mauris iaculis sed odio et accumsan. In sed ligula lacus.\n" +
                            "\n" +
                            "Mauris ut purus ac velit convallis egestas ac at dui. Suspendisse potenti. Donec vulputate, augue ac interdum feugiat, sem magna blandit elit, fringilla efficitur nibh nibh ac dolor. In auctor lacus ipsum, eu mattis neque convallis et. Praesent risus arcu, accumsan sit amet tincidunt non, feugiat eu augue. Mauris felis diam, vestibulum vel sem quis, tincidunt finibus ipsum. Suspendisse et luctus ante. Proin erat metus, posuere sed sagittis at, rutrum sit amet elit. Praesent porttitor suscipit mauris a hendrerit. Curabitur vitae est turpis. Phasellus vel urna vitae libero placerat tincidunt. In elit risus, tincidunt at cursus sit amet, lobortis sit amet lorem. Morbi at arcu sit amet erat consectetur vulputate.\n" +
                            "\n" +
                            "Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Ut dictum ligula vel ante ultricies, sit amet dictum nisi convallis. Curabitur vestibulum leo in elementum ultricies. Suspendisse erat ipsum, fermentum id dignissim dignissim, dictum sit amet justo. Praesent gravida tortor quis auctor accumsan. Mauris vel dolor non ante pulvinar accumsan mollis in sem. Mauris non consequat mi. Donec volutpat lobortis dignissim. Nullam tristique faucibus cursus. Nullam tristique dictum urna eu congue. Curabitur ut metus vestibulum massa dignissim congue. Vestibulum scelerisque turpis eu mauris consectetur, ac efficitur mi tempus. Curabitur ornare felis turpis, tristique ullamcorper lacus cursus quis.\n" +
                            "\n" +
                            "Integer eget tortor vulputate, aliquam felis ac, tempor nulla. Sed et sem in elit sollicitudin ullamcorper. Morbi facilisis, quam sed ultrices sagittis, purus risus tempor lorem, at rutrum enim orci fringilla urna. Vestibulum at lacus dictum erat varius egestas. Nam vel vestibulum mauris, ut ullamcorper ante. Aliquam dictum eros et massa lacinia, quis dapibus ipsum interdum. Ut mauris leo, ultrices a tempus a, mattis at lorem. Cras consequat facilisis sollicitudin. Aenean in faucibus ante. Nam in vehicula leo, ut semper risus.\n" +
                            "\n" +
                            "Sed purus risus, volutpat et nunc ac, aliquam pellentesque nulla. Praesent vel nibh at lorem porttitor faucibus. Sed ut nunc scelerisque, finibus augue hendrerit, rutrum sapien. Aliquam erat volutpat. Donec tincidunt maximus nulla, imperdiet ornare leo volutpat id. In non lorem ornare, volutpat nisi vitae, convallis sem. Aenean molestie dapibus tortor, ac elementum nisi varius eget.\n" +
                            "\n" +
                            "Nam eget felis dolor. Phasellus urna enim, elementum ac magna rutrum, efficitur luctus nisl. In dictum nibh metus, vel malesuada nibh tincidunt bibendum. Cras in purus a elit ultricies volutpat. Sed interdum velit ac dolor dictum, faucibus placerat orci imperdiet. Morbi a ligula quis leo ornare elementum. Proin quis porttitor tellus. Maecenas eleifend porttitor lacus a ultrices. Phasellus eleifend in leo eu porttitor. Nam vel venenatis erat. Nullam magna felis, vehicula a ligula nec, iaculis mollis arcu.\n" +
                            "\n" +
                            "Nunc ultrices turpis et porta lacinia. Nunc semper lorem lectus, sed ornare justo aliquam sit amet. Etiam convallis turpis massa, quis imperdiet sapien auctor nec. Ut non ipsum ut orci commodo semper. Curabitur pellentesque, libero id faucibus pellentesque, arcu purus consectetur velit, eu varius elit nunc sed sapien. Duis nec nisi odio. Nullam commodo, enim eget efficitur luctus, mi purus sagittis ante, a accumsan augue sem nec urna. Integer id ex tellus.\n" +
                            "\n" +
                            "Integer vel varius lacus. Vestibulum magna augue, convallis condimentum dapibus ut, tempor in sem. Ut semper sagittis dolor non maximus. Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Proin sit amet molestie ligula, eget tincidunt eros. Nullam ex enim, dignissim ac consequat ac, varius at ipsum. Nulla faucibus mi nisi, sed facilisis leo efficitur a.\n" +
                            "\n" +
                            "Donec a tempor eros. Donec et nisl velit. Integer venenatis vel enim at tristique. Phasellus sed lacus congue, consequat urna vel, gravida magna. Maecenas pellentesque augue in felis lacinia, cursus porta ipsum iaculis. Nullam varius ultrices mi eu gravida. Nunc ac ipsum volutpat nisl viverra pellentesque.\n" +
                            "\n" +
                            "Aliquam purus erat, hendrerit a nulla at, pulvinar mollis ante. Sed mattis rutrum augue blandit egestas. Nunc eu lobortis risus. Maecenas condimentum nisi eget nisi convallis scelerisque. Suspendisse ultricies, nibh ac tempor sagittis, sapien metus placerat neque, eu cursus ipsum ipsum id justo. Quisque non porttitor eros. Nunc nunc dui, maximus sit amet mollis ac, molestie ut enim.\n" +
                            "\n" +
                            "Praesent turpis tellus, egestas eu elementum id, placerat sed augue. Maecenas tincidunt placerat bibendum. Nulla facilisis leo sed massa fringilla euismod. Donec gravida felis eu lectus ultrices, fermentum tristique sem lacinia. Praesent nec elementum est, sed gravida est. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Fusce pretium sollicitudin ligula, eget pharetra leo molestie eget. Nam vulputate sem nisl, eu egestas tortor gravida ut. Donec egestas, risus at posuere dapibus, turpis elit tempor magna, feugiat pretium quam elit gravida libero. Donec lobortis purus ex, in ultricies odio rhoncus vel. In ornare, ipsum in convallis vehicula, leo mi dictum arcu, tempus efficitur purus ligula sit amet libero. In neque purus, efficitur sed dolor nec, sodales mattis augue. Sed quis orci vel magna posuere porttitor. Donec facilisis vehicula tempus. Curabitur tincidunt dignissim mi at finibus. Fusce dapibus purus eget massa dapibus, in consectetur urna pharetra.\n" +
                            "\n" +
                            "Duis ultricies mauris non orci tempus bibendum. Morbi imperdiet eu erat eget feugiat. Nullam volutpat nulla sit amet metus convallis, a auctor enim bibendum. In vel euismod velit, et hendrerit ante. Proin ac est tincidunt, ullamcorper quam vitae, luctus diam. Vestibulum ut elit nec arcu congue semper dapibus in lectus. Quisque est leo, finibus non neque eget, tincidunt ultricies augue. Suspendisse sodales consectetur tellus, at maximus quam cursus ut. Nulla ullamcorper enim ut semper aliquet. Mauris dictum nisi ligula, vitae laoreet dui scelerisque sit amet. Nulla luctus dui a lacus elementum, at porttitor erat sodales.\n" +
                            "\n" +
                            "Curabitur et lorem tellus. Ut efficitur nunc ante, non scelerisque massa malesuada et. Duis eget sapien sem. Nulla erat justo, venenatis vel nibh sit amet, dapibus faucibus quam. Ut leo erat, molestie at risus vitae, finibus egestas augue. Mauris sodales dui nec ante porttitor, aliquam pharetra arcu interdum. Nunc in enim nec nulla mollis sodales vitae eget dui. Duis eu egestas mi, vitae tincidunt nulla. Ut nec turpis leo.Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aenean in diam ligula. Phasellus eleifend efficitur tortor vulputate maximus. Aliquam fringilla sem at ornare efficitur. Sed id nisi non tellus ultricies ultrices. Morbi hendrerit magna quis ante egestas hendrerit. Nulla luctus viverra ultricies. Vivamus libero neque, eleifend sed condimentum non, pretium id nunc. Sed porta pellentesque ipsum, quis convallis tellus suscipit sit amet. Phasellus in luctus leo. Etiam rhoncus odio urna, vel volutpat elit dapibus at.\n" +
                            "\n" +
                            "Nam iaculis, erat sed luctus facilisis, diam tellus porta urna, id convallis orci ipsum quis libero. Cras finibus porta fringilla. Nunc et efficitur urna. Nulla nulla magna, malesuada eget erat vel, tempor consequat ipsum. Suspendisse nec mollis leo. Duis elit velit, tempor ut egestas et, sagittis at odio. Duis iaculis, ex nec convallis rhoncus, nunc mi finibus magna, ut scelerisque lorem ex cursus tortor. Praesent vel velit turpis. Nunc ac nisi dignissim, hendrerit metus vehicula, venenatis elit. Vivamus efficitur odio eu malesuada ornare. Suspendisse ligula lacus, rutrum id finibus eu, pulvinar vel turpis. Curabitur tellus quam, malesuada nec nisl et, pulvinar tempor lacus. Proin eget lorem iaculis nisi venenatis condimentum. Aliquam erat volutpat. Curabitur fringilla, lorem in rhoncus ornare, magna elit elementum lorem, non rutrum ex lectus quis quam.\n" +
                            "\n" +
                            "Nam molestie, mi non cursus egestas, neque ex mattis nunc, in lobortis ex arcu at libero. Curabitur hendrerit, leo at ultrices luctus, sapien turpis interdum velit, nec pharetra metus elit et velit. Praesent eget ullamcorper augue. Mauris commodo, lorem at efficitur scelerisque, libero sapien consectetur mi, eget tempor metus nisi egestas sapien. Nulla aliquet felis leo, vitae porta diam vulputate sed. Curabitur finibus tellus eget vulputate pretium. Mauris porttitor venenatis faucibus. In tincidunt tellus id dictum scelerisque. Nulla gravida pharetra condimentum. Phasellus eros velit, lacinia in imperdiet et, porttitor sit amet urna. Interdum et malesuada fames ac ante ipsum primis in faucibus. Nulla ac viverra nulla. Nunc lobortis eros nec lectus convallis, id iaculis metus tristique. Mauris iaculis sed odio et accumsan. In sed ligula lacus.\n" +
                            "\n" +
                            "Mauris ut purus ac velit convallis egestas ac at dui. Suspendisse potenti. Donec vulputate, augue ac interdum feugiat, sem magna blandit elit, fringilla efficitur nibh nibh ac dolor. In auctor lacus ipsum, eu mattis neque convallis et. Praesent risus arcu, accumsan sit amet tincidunt non, feugiat eu augue. Mauris felis diam, vestibulum vel sem quis, tincidunt finibus ipsum. Suspendisse et luctus ante. Proin erat metus, posuere sed sagittis at, rutrum sit amet elit. Praesent porttitor suscipit mauris a hendrerit. Curabitur vitae est turpis. Phasellus vel urna vitae libero placerat tincidunt. In elit risus, tincidunt at cursus sit amet, lobortis sit amet lorem. Morbi at arcu sit amet erat consectetur vulputate.\n" +
                            "\n" +
                            "Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Ut dictum ligula vel ante ultricies, sit amet dictum nisi convallis. Curabitur vestibulum leo in elementum ultricies. Suspendisse erat ipsum, fermentum id dignissim dignissim, dictum sit amet justo. Praesent gravida tortor quis auctor accumsan. Mauris vel dolor non ante pulvinar accumsan mollis in sem. Mauris non consequat mi. Donec volutpat lobortis dignissim. Nullam tristique faucibus cursus. Nullam tristique dictum urna eu congue. Curabitur ut metus vestibulum massa dignissim congue. Vestibulum scelerisque turpis eu mauris consectetur, ac efficitur mi tempus. Curabitur ornare felis turpis, tristique ullamcorper lacus cursus quis.\n" +
                            "\n" +
                            "Integer eget tortor vulputate, aliquam felis ac, tempor nulla. Sed et sem in elit sollicitudin ullamcorper. Morbi facilisis, quam sed ultrices sagittis, purus risus tempor lorem, at rutrum enim orci fringilla urna. Vestibulum at lacus dictum erat varius egestas. Nam vel vestibulum mauris, ut ullamcorper ante. Aliquam dictum eros et massa lacinia, quis dapibus ipsum interdum. Ut mauris leo, ultrices a tempus a, mattis at lorem. Cras consequat facilisis sollicitudin. Aenean in faucibus ante. Nam in vehicula leo, ut semper risus.\n" +
                            "\n" +
                            "Sed purus risus, volutpat et nunc ac, aliquam pellentesque nulla. Praesent vel nibh at lorem porttitor faucibus. Sed ut nunc scelerisque, finibus augue hendrerit, rutrum sapien. Aliquam erat volutpat. Donec tincidunt maximus nulla, imperdiet ornare leo volutpat id. In non lorem ornare, volutpat nisi vitae, convallis sem. Aenean molestie dapibus tortor, ac elementum nisi varius eget.\n" +
                            "\n" +
                            "Nam eget felis dolor. Phasellus urna enim, elementum ac magna rutrum, efficitur luctus nisl. In dictum nibh metus, vel malesuada nibh tincidunt bibendum. Cras in purus a elit ultricies volutpat. Sed interdum velit ac dolor dictum, faucibus placerat orci imperdiet. Morbi a ligula quis leo ornare elementum. Proin quis porttitor tellus. Maecenas eleifend porttitor lacus a ultrices. Phasellus eleifend in leo eu porttitor. Nam vel venenatis erat. Nullam magna felis, vehicula a ligula nec, iaculis mollis arcu.\n" +
                            "\n" +
                            "Nunc ultrices turpis et porta lacinia. Nunc semper lorem lectus, sed ornare justo aliquam sit amet. Etiam convallis turpis massa, quis imperdiet sapien auctor nec. Ut non ipsum ut orci commodo semper. Curabitur pellentesque, libero id faucibus pellentesque, arcu purus consectetur velit, eu varius elit nunc sed sapien. Duis nec nisi odio. Nullam commodo, enim eget efficitur luctus, mi purus sagittis ante, a accumsan augue sem nec urna. Integer id ex tellus.\n" +
                            "\n" +
                            "Integer vel varius lacus. Vestibulum magna augue, convallis condimentum dapibus ut, tempor in sem. Ut semper sagittis dolor non maximus. Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Proin sit amet molestie ligula, eget tincidunt eros. Nullam ex enim, dignissim ac consequat ac, varius at ipsum. Nulla faucibus mi nisi, sed facilisis leo efficitur a.\n" +
                            "\n" +
                            "Donec a tempor eros. Donec et nisl velit. Integer venenatis vel enim at tristique. Phasellus sed lacus congue, consequat urna vel, gravida magna. Maecenas pellentesque augue in felis lacinia, cursus porta ipsum iaculis. Nullam varius ultrices mi eu gravida. Nunc ac ipsum volutpat nisl viverra pellentesque.\n" +
                            "\n" +
                            "Aliquam purus erat, hendrerit a nulla at, pulvinar mollis ante. Sed mattis rutrum augue blandit egestas. Nunc eu lobortis risus. Maecenas condimentum nisi eget nisi convallis scelerisque. Suspendisse ultricies, nibh ac tempor sagittis, sapien metus placerat neque, eu cursus ipsum ipsum id justo. Quisque non porttitor eros. Nunc nunc dui, maximus sit amet mollis ac, molestie ut enim.\n" +
                            "\n" +
                            "Praesent turpis tellus, egestas eu elementum id, placerat sed augue. Maecenas tincidunt placerat bibendum. Nulla facilisis leo sed massa fringilla euismod. Donec gravida felis eu lectus ultrices, fermentum tristique sem lacinia. Praesent nec elementum est, sed gravida est. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Fusce pretium sollicitudin ligula, eget pharetra leo molestie eget. Nam vulputate sem nisl, eu egestas tortor gravida ut. Donec egestas, risus at posuere dapibus, turpis elit tempor magna, feugiat pretium quam elit gravida libero. Donec lobortis purus ex, in ultricies odio rhoncus vel. In ornare, ipsum in convallis vehicula, leo mi dictum arcu, tempus efficitur purus ligula sit amet libero. In neque purus, efficitur sed dolor nec, sodales mattis augue. Sed quis orci vel magna posuere porttitor. Donec facilisis vehicula tempus. Curabitur tincidunt dignissim mi at finibus. Fusce dapibus purus eget massa dapibus, in consectetur urna pharetra.\n" +
                            "\n" +
                            "Duis ultricies mauris non orci tempus bibendum. Morbi imperdiet eu erat eget feugiat. Nullam volutpat nulla sit amet metus convallis, a auctor enim bibendum. In vel euismod velit, et hendrerit ante. Proin ac est tincidunt, ullamcorper quam vitae, luctus diam. Vestibulum ut elit nec arcu congue semper dapibus in lectus. Quisque est leo, finibus non neque eget, tincidunt ultricies augue. Suspendisse sodales consectetur tellus, at maximus quam cursus ut. Nulla ullamcorper enim ut semper aliquet. Mauris dictum nisi ligula, vitae laoreet dui scelerisque sit amet. Nulla luctus dui a lacus elementum, at porttitor erat sodales.\n" +
                            "\n" +
                            "Curabitur et lorem tellus. Ut efficitur nunc ante, non scelerisque massa malesuada et. Duis eget sapien sem. Nulla erat justo, venenatis vel nibh sit amet, dapibus faucibus quam. Ut leo erat, molestie at risus vitae, finibus egestas augue. Mauris sodales dui nec ante porttitor, aliquam pharetra arcu interdum. Nunc in enim nec nulla mollis sodales vitae eget dui. Duis eu egestas mi, vitae tincidunt nulla. Ut nec turpis leo.Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aenean in diam ligula. Phasellus eleifend efficitur tortor vulputate maximus. Aliquam fringilla sem at ornare efficitur. Sed id nisi non tellus ultricies ultrices. Morbi hendrerit magna quis ante egestas hendrerit. Nulla luctus viverra ultricies. Vivamus libero neque, eleifend sed condimentum non, pretium id nunc. Sed porta pellentesque ipsum, quis convallis tellus suscipit sit amet. Phasellus in luctus leo. Etiam rhoncus odio urna, vel volutpat elit dapibus at.\n" +
                            "\n" +
                            "Nam iaculis, erat sed luctus facilisis, diam tellus porta urna, id convallis orci ipsum quis libero. Cras finibus porta fringilla. Nunc et efficitur urna. Nulla nulla magna, malesuada eget erat vel, tempor consequat ipsum. Suspendisse nec mollis leo. Duis elit velit, tempor ut egestas et, sagittis at odio. Duis iaculis, ex nec convallis rhoncus, nunc mi finibus magna, ut scelerisque lorem ex cursus tortor. Praesent vel velit turpis. Nunc ac nisi dignissim, hendrerit metus vehicula, venenatis elit. Vivamus efficitur odio eu malesuada ornare. Suspendisse ligula lacus, rutrum id finibus eu, pulvinar vel turpis. Curabitur tellus quam, malesuada nec nisl et, pulvinar tempor lacus. Proin eget lorem iaculis nisi venenatis condimentum. Aliquam erat volutpat. Curabitur fringilla, lorem in rhoncus ornare, magna elit elementum lorem, non rutrum ex lectus quis quam.\n" +
                            "\n" +
                            "Nam molestie, mi non cursus egestas, neque ex mattis nunc, in lobortis ex arcu at libero. Curabitur hendrerit, leo at ultrices luctus, sapien turpis interdum velit, nec pharetra metus elit et velit. Praesent eget ullamcorper augue. Mauris commodo, lorem at efficitur scelerisque, libero sapien consectetur mi, eget tempor metus nisi egestas sapien. Nulla aliquet felis leo, vitae porta diam vulputate sed. Curabitur finibus tellus eget vulputate pretium. Mauris porttitor venenatis faucibus. In tincidunt tellus id dictum scelerisque. Nulla gravida pharetra condimentum. Phasellus eros velit, lacinia in imperdiet et, porttitor sit amet urna. Interdum et malesuada fames ac ante ipsum primis in faucibus. Nulla ac viverra nulla. Nunc lobortis eros nec lectus convallis, id iaculis metus tristique. Mauris iaculis sed odio et accumsan. In sed ligula lacus.\n" +
                            "\n" +
                            "Mauris ut purus ac velit convallis egestas ac at dui. Suspendisse potenti. Donec vulputate, augue ac interdum feugiat, sem magna blandit elit, fringilla efficitur nibh nibh ac dolor. In auctor lacus ipsum, eu mattis neque convallis et. Praesent risus arcu, accumsan sit amet tincidunt non, feugiat eu augue. Mauris felis diam, vestibulum vel sem quis, tincidunt finibus ipsum. Suspendisse et luctus ante. Proin erat metus, posuere sed sagittis at, rutrum sit amet elit. Praesent porttitor suscipit mauris a hendrerit. Curabitur vitae est turpis. Phasellus vel urna vitae libero placerat tincidunt. In elit risus, tincidunt at cursus sit amet, lobortis sit amet lorem. Morbi at arcu sit amet erat consectetur vulputate.\n" +
                            "\n" +
                            "Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Ut dictum ligula vel ante ultricies, sit amet dictum nisi convallis. Curabitur vestibulum leo in elementum ultricies. Suspendisse erat ipsum, fermentum id dignissim dignissim, dictum sit amet justo. Praesent gravida tortor quis auctor accumsan. Mauris vel dolor non ante pulvinar accumsan mollis in sem. Mauris non consequat mi. Donec volutpat lobortis dignissim. Nullam tristique faucibus cursus. Nullam tristique dictum urna eu congue. Curabitur ut metus vestibulum massa dignissim congue. Vestibulum scelerisque turpis eu mauris consectetur, ac efficitur mi tempus. Curabitur ornare felis turpis, tristique ullamcorper lacus cursus quis.\n" +
                            "\n" +
                            "Integer eget tortor vulputate, aliquam felis ac, tempor nulla. Sed et sem in elit sollicitudin ullamcorper. Morbi facilisis, quam sed ultrices sagittis, purus risus tempor lorem, at rutrum enim orci fringilla urna. Vestibulum at lacus dictum erat varius egestas. Nam vel vestibulum mauris, ut ullamcorper ante. Aliquam dictum eros et massa lacinia, quis dapibus ipsum interdum. Ut mauris leo, ultrices a tempus a, mattis at lorem. Cras consequat facilisis sollicitudin. Aenean in faucibus ante. Nam in vehicula leo, ut semper risus.\n" +
                            "\n" +
                            "Sed purus risus, volutpat et nunc ac, aliquam pellentesque nulla. Praesent vel nibh at lorem porttitor faucibus. Sed ut nunc scelerisque, finibus augue hendrerit, rutrum sapien. Aliquam erat volutpat. Donec tincidunt maximus nulla, imperdiet ornare leo volutpat id. In non lorem ornare, volutpat nisi vitae, convallis sem. Aenean molestie dapibus tortor, ac elementum nisi varius eget.\n" +
                            "\n" +
                            "Nam eget felis dolor. Phasellus urna enim, elementum ac magna rutrum, efficitur luctus nisl. In dictum nibh metus, vel malesuada nibh tincidunt bibendum. Cras in purus a elit ultricies volutpat. Sed interdum velit ac dolor dictum, faucibus placerat orci imperdiet. Morbi a ligula quis leo ornare elementum. Proin quis porttitor tellus. Maecenas eleifend porttitor lacus a ultrices. Phasellus eleifend in leo eu porttitor. Nam vel venenatis erat. Nullam magna felis, vehicula a ligula nec, iaculis mollis arcu.\n" +
                            "\n" +
                            "Nunc ultrices turpis et porta lacinia. Nunc semper lorem lectus, sed ornare justo aliquam sit amet. Etiam convallis turpis massa, quis imperdiet sapien auctor nec. Ut non ipsum ut orci commodo semper. Curabitur pellentesque, libero id faucibus pellentesque, arcu purus consectetur velit, eu varius elit nunc sed sapien. Duis nec nisi odio. Nullam commodo, enim eget efficitur luctus, mi purus sagittis ante, a accumsan augue sem nec urna. Integer id ex tellus.\n" +
                            "\n" +
                            "Integer vel varius lacus. Vestibulum magna augue, convallis condimentum dapibus ut, tempor in sem. Ut semper sagittis dolor non maximus. Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Proin sit amet molestie ligula, eget tincidunt eros. Nullam ex enim, dignissim ac consequat ac, varius at ipsum. Nulla faucibus mi nisi, sed facilisis leo efficitur a.\n" +
                            "\n" +
                            "Donec a tempor eros. Donec et nisl velit. Integer venenatis vel enim at tristique. Phasellus sed lacus congue, consequat urna vel, gravida magna. Maecenas pellentesque augue in felis lacinia, cursus porta ipsum iaculis. Nullam varius ultrices mi eu gravida. Nunc ac ipsum volutpat nisl viverra pellentesque.\n" +
                            "\n" +
                            "Aliquam purus erat, hendrerit a nulla at, pulvinar mollis ante. Sed mattis rutrum augue blandit egestas. Nunc eu lobortis risus. Maecenas condimentum nisi eget nisi convallis scelerisque. Suspendisse ultricies, nibh ac tempor sagittis, sapien metus placerat neque, eu cursus ipsum ipsum id justo. Quisque non porttitor eros. Nunc nunc dui, maximus sit amet mollis ac, molestie ut enim.\n" +
                            "\n" +
                            "Praesent turpis tellus, egestas eu elementum id, placerat sed augue. Maecenas tincidunt placerat bibendum. Nulla facilisis leo sed massa fringilla euismod. Donec gravida felis eu lectus ultrices, fermentum tristique sem lacinia. Praesent nec elementum est, sed gravida est. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Fusce pretium sollicitudin ligula, eget pharetra leo molestie eget. Nam vulputate sem nisl, eu egestas tortor gravida ut. Donec egestas, risus at posuere dapibus, turpis elit tempor magna, feugiat pretium quam elit gravida libero. Donec lobortis purus ex, in ultricies odio rhoncus vel. In ornare, ipsum in convallis vehicula, leo mi dictum arcu, tempus efficitur purus ligula sit amet libero. In neque purus, efficitur sed dolor nec, sodales mattis augue. Sed quis orci vel magna posuere porttitor. Donec facilisis vehicula tempus. Curabitur tincidunt dignissim mi at finibus. Fusce dapibus purus eget massa dapibus, in consectetur urna pharetra.\n" +
                            "\n" +
                            "Duis ultricies mauris non orci tempus bibendum. Morbi imperdiet eu erat eget feugiat. Nullam volutpat nulla sit amet metus convallis, a auctor enim bibendum. In vel euismod velit, et hendrerit ante. Proin ac est tincidunt, ullamcorper quam vitae, luctus diam. Vestibulum ut elit nec arcu congue semper dapibus in lectus. Quisque est leo, finibus non neque eget, tincidunt ultricies augue. Suspendisse sodales consectetur tellus, at maximus quam cursus ut. Nulla ullamcorper enim ut semper aliquet. Mauris dictum nisi ligula, vitae laoreet dui scelerisque sit amet. Nulla luctus dui a lacus elementum, at porttitor erat sodales.\n" +
                            "\n" +
                            "Curabitur et lorem tellus. Ut efficitur nunc ante, non scelerisque massa malesuada et. Duis eget sapien sem. Nulla erat justo, venenatis vel nibh sit amet, dapibus faucibus quam. Ut leo erat, molestie at risus vitae, finibus egestas augue. Mauris sodales dui nec ante porttitor, aliquam pharetra arcu interdum. Nunc in enim nec nulla mollis sodales vitae eget dui. Duis eu egestas mi, vitae tincidunt nulla. Ut nec turpis leo. 1 2 3 3 2 1"));
                }
            }, 15);
            System.out.println("Sended data");
        });

      //  server.linkLoadBalancer(new BalancerConfiguration("localhost", 80, "testpassword2"));


        server.registerEndpoint("/api/v2/getplayer/?name", ((request, settings) -> {
            String username = request.getVariable("name");
            String data = "{\n" +
                    "  \"name\": \"usr\",\n" +
                    "  \"score\": 5,\n" +
                    "  \"kills\": 6,\n" +
                    "  \"coins\": 1,\n" +
                    "  \"online\": true\n" +
                    "}";
            data = data.replace("usr", username);

            settings.setContent(HttpContentType.JSON);

            return data;
        }));

        server.registerEndpoint("/time", (req, settings) -> {
            return "The server time is: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        });
        server.registerEndpoint("/hi/?name", (req, settings)  -> {
            return "Welcome to FireIO " + req.getVariable("name") + "!";
        });

        server.registerEndpoint("/hi", (req, settings)  -> {
            return "hoi";
        });
    }

}
