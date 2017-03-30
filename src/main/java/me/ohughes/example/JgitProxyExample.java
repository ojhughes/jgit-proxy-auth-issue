package me.ohughes.example;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.HttpTransport;
import org.eclipse.jgit.transport.http.apache.HttpClientConnectionFactory;
import org.eclipse.jgit.util.CachedAuthenticator;
import org.eclipse.jgit.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Small sample to validate if JGit can clone a repo through an authenticated proxy
 */
public class JgitProxyExample {

    private static final String HTTPS_PROXY_HOST_KEY = "https.proxyHost";
    private static final String HTTPS_PROXY_PORT_KEY = "https.proxyPort";
    private static final String HTTPS_PROXY_USER_KEY = "https.proxyUser";
    private static final String HTTPS_PROXY_PASS_KEY = "https.proxyPassword";
    private static final String CLONE_PATH = "/tmp/jgit-proxy";
    private String proxyHost;
    private int proxyPort;
    private String proxyUser;
    private String proxyPassword;

    public JgitProxyExample(String proxyHost, int proxyPort, String proxyUser, String proxyPassword) {
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.proxyUser = proxyUser;
        this.proxyPassword = proxyPassword;
    }

    public static void main(String[] args) throws Exception {
        HttpTransport.setConnectionFactory(new HttpClientConnectionFactory());
        Path path = Paths.get(CLONE_PATH);
        try {

            Path clonePath = Files.createDirectories(path);
            JgitProxyExample jgitProxyExample = new JgitProxyExample(
                    args[0],
                    Integer.parseInt(args[1]),
                    args[2],
                    args[3]
            );

            jgitProxyExample.configureProxy();
            Git git = jgitProxyExample.cloneRepo("https://github.com/ojhughes/config-source-2", clonePath.toFile());
            System.out.println("Git branches" + git.branchList().call().toString());
        } catch (IOException | GitAPIException e) {
            e.printStackTrace();
        } finally {

            if (Files.exists(path)) {
                FileUtils.delete(path.toFile(), FileUtils.RECURSIVE);
            }
        }
    }

    private void configureProxy() {
        System.setProperty(HTTPS_PROXY_HOST_KEY, this.proxyHost);
        System.setProperty(HTTPS_PROXY_PORT_KEY, String.valueOf(this.proxyPort));
        System.setProperty(HTTPS_PROXY_USER_KEY, this.proxyUser);
        System.setProperty(HTTPS_PROXY_PASS_KEY, this.proxyPassword);
        Authenticator.setDefault(new ProxyAuthenticator(this.proxyUser, this.proxyPassword));
    }

    private Git cloneRepo(String uri, File clonePath) throws GitAPIException {
        CloneCommand clone = Git.cloneRepository();
        clone.setDirectory(clonePath).setURI(uri);
        return clone.call();
    }

    public static class ProxyAuthenticator extends Authenticator {

        private final PasswordAuthentication passwordAuthentication;

        private ProxyAuthenticator(String username, String password) {
            this.passwordAuthentication = new PasswordAuthentication(username,
                    password.toCharArray());
        }

        protected PasswordAuthentication getPasswordAuthentication() {
            return this.passwordAuthentication;
        }

    }
}


