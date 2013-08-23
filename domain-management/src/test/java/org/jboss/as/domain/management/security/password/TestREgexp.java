package org.jboss.as.domain.management.security.password;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author bloemgracht
 */
public class TestRegexp {

    @Test
    public void test() {
        final RegexRestriction regexRestriction = new RegexRestriction("(.*[a-zA-Z].*){2}", "");
        Assert.assertTrue(regexRestriction.pass("aa"));
        Assert.assertFalse(regexRestriction.pass("a"));
        Assert.assertTrue(regexRestriction.pass("1aa22"));
        Assert.assertTrue(regexRestriction.pass("1a2b2"));
    }
}
