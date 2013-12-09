package com.interact.listen

//import
import org.apache.commons.lang.StringUtils
import org.ocpsoft.prettytime.*
import org.ocpsoft.prettytime.format.SimpleTimeFormat
import org.ocpsoft.prettytime.units.*

// copied from the prettytime plugin so that we can replace
// the JustNow instance that gets used for formatting
class ConfigurablePrettyTimeTagLib {

    static namespace = "listen"

    def prettytime = {attrs, body ->
        def date = attrs.remove('date')
        def showTime = Boolean.valueOf(attrs.remove('showTime'))
        def capitalize = Boolean.valueOf(attrs.remove('capitalize'))

        if (!date) throw new Exception(
                "There must be a 'date' attribute included in the prettytime tag.")
        if ('org.joda.time.DateTime' == date.class.name) {
            date = date.toDate()
        }

        def justNow = new JustNow()
        justNow.setMaxQuantity(60000)

        PrettyTime prettyTime = new PrettyTime();
        prettyTime.registerUnit(justNow, justNowFormatToI18n());
        Millisecond thisMilli = new Millisecond();
        prettyTime.registerUnit(thisMilli, formatToI18n(thisMilli));
        Second thisSecond = new Second();
        prettyTime.registerUnit(thisSecond, formatToI18n(thisSecond));
        Minute thisMinute = new Minute();
        prettyTime.registerUnit(thisMinute, formatToI18n(thisMinute));
        Hour thisHour = new Hour();
        prettyTime.registerUnit(thisHour, formatToI18n(thisHour));
        Day thisDay = new Day();
        prettyTime.registerUnit(thisDay, formatToI18n(thisDay));
        Week thisWeek = new Week();
        prettyTime.registerUnit(thisWeek, formatToI18n(thisWeek));
        Month thisMonth = new Month();
        prettyTime.registerUnit(thisMonth, formatToI18n(thisMonth));
        Year thisYear = new Year();
        prettyTime.registerUnit(thisYear, formatToI18n(thisYear));
        Decade thisDecade = new Decade();
        prettyTime.registerUnit(thisDecade, formatToI18n(thisDecade));
        Century thisCentury = new Century();
        prettyTime.registerUnit(thisCentury, formatToI18n(thisCentury));
        Millennium thisMillennium = new Millennium();
        prettyTime.registerUnit(thisMillennium, formatToI18n(thisMillennium));

/*        prettyTime.units = [
                justNowUnitToI18n(justNow),
                unitToI18n(new Millisecond()),
                unitToI18n(new Second()),
                unitToI18n(new Minute()),
                unitToI18n(new Hour()),
                unitToI18n(new Day()),
                unitToI18n(new Week()),
                unitToI18n(new Month()),
                unitToI18n(new Year()),
                unitToI18n(new Decade()),
                unitToI18n(new Century()),
                unitToI18n(new Millennium())
        ] as Map<TimeUnit, TimeFormat>*/

        String result = prettyTime.format(date).trim()
        if (capitalize) result = StringUtils.capitalize(result)
        if (showTime) {
            def format = attrs.remove('format') ?: 
                            message(code: 'prettytime.date.format', default: 'hh:mm:ss a')
            result += ', ' + date.format(format)
        }

        out << result
    }

    private TimeFormat formatToI18n(TimeUnit unit)
    {
        SimpleTimeFormat stf = new SimpleTimeFormat();
        stf.setPattern(' %n %u ');
        stf.setPastSuffix(g.message(code: 'prettytime.justnow.past.suffix').toString());
        stf.setFutureSuffix(g.message(code: 'prettytime.justnow.future.suffix').toString());

        // name/pluralName
        def className = unit.class.name;
        className = className[className.lastIndexOf('.') + 1..-1].toLowerCase();
        stf.setSingularName(g.message(code: "prettytime.$className").toString());
        stf.setPluralName(g.message(code: "prettytime.${className}s").toString());
        stf.setPastPrefix(g.message(code: 'prettytime.past.prefix').toString())
        // preffix/suffix
        stf.setPastSuffix(g.message(code: 'prettytime.past.suffix').toString());
        stf.setFuturePrefix(g.message(code: 'prettytime.future.prefix').toString());
        stf.setFutureSuffix(g.message(code: 'prettytime.future.suffix'));

        return stf;
    }

    private def unitToI18n(unit) {
        // pattern
        unit.format.pattern = ' %n %u '
        // name/pluralName
        def className = unit.class.name
        className = className[className.lastIndexOf('.') + 1..-1].toLowerCase()
        unit.name = g.message(code: "prettytime.$className")
        unit.pluralName = g.message(code: "prettytime.${className}s")
        // preffix/suffix
        unit.format.pastPrefix = g.message(code: 'prettytime.past.prefix')
        unit.format.pastSuffix = g.message(code: 'prettytime.past.suffix')
        unit.format.futurePrefix = g.message(code: 'prettytime.future.prefix')
        unit.format.futureSuffix = g.message(code: 'prettytime.future.suffix')
        unit
    }

    private def justNowUnitToI18n(unit)
    {
        // pattern
        unit.format.pattern = ' %u '
        // preffix/suffix
        unit.format.pastSuffix = g.message(code: 'prettytime.justnow.past.suffix')
        unit.format.futureSuffix = g.message(code: 'prettytime.justnow.future.suffix')
        unit
    }

    private TimeFormat justNowFormatToI18n()
    {
        SimpleTimeFormat stf = new SimpleTimeFormat();
        stf.setPattern(' %u ');
        stf.setPastSuffix(g.message(code: 'prettytime.justnow.past.suffix'));
        stf.setFutureSuffix(g.message(code: 'prettytime.justnow.future.suffix'));

        return stf;
    }

}
