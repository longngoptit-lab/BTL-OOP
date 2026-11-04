package qlht.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import qlht.model.LichHoc;
import qlht.util.Const;
import qlht.util.TimeUtil;

/** Xếp các buổi học chồng giờ vào những làn riêng, giữ đúng tỷ lệ thời gian. */
public final class WeekScheduleLayout {
    private WeekScheduleLayout() { }

    public record Slot(LichHoc lesson, int day, int lane, int laneCount, int start, int end) { }

    public static List<Slot> arrange(List<LichHoc> lessons) {
        List<Slot> result = new ArrayList<>();
        for (int day = 0; day < Const.DAYS.size(); day++) {
            String name = Const.DAYS.get(day);
            List<LichHoc> sorted = lessons.stream().filter(l -> name.equals(l.getThu()))
                    .filter(l -> l.getTietBd() <= l.getTietKt() && TimeUtil.khoangGioTiet(l.getTietBd(), l.getTietKt()) != null)
                    .sorted(Comparator.comparingInt(LichHoc::getTietBd).thenComparingInt(LichHoc::getTietKt).thenComparingInt(LichHoc::getId))
                    .toList();
            List<Slot> group = new ArrayList<>();
            List<Integer> ends = new ArrayList<>();
            int groupEnd = -1;
            for (LichHoc lesson : sorted) {
                int[] time = TimeUtil.khoangGioTiet(lesson.getTietBd(), lesson.getTietKt());
                if (!group.isEmpty() && time[0] >= groupEnd) {
                    finish(result, group, ends.size()); group.clear(); ends.clear(); groupEnd = -1;
                }
                int lane = 0;
                while (lane < ends.size() && ends.get(lane) > time[0]) lane++;
                if (lane == ends.size()) ends.add(time[1]); else ends.set(lane, time[1]);
                group.add(new Slot(lesson, day, lane, 0, time[0], time[1]));
                groupEnd = Math.max(groupEnd, time[1]);
            }
            finish(result, group, ends.size());
        }
        return List.copyOf(result);
    }

    private static void finish(List<Slot> result, List<Slot> group, int lanes) {
        for (Slot s : group) result.add(new Slot(s.lesson(), s.day(), s.lane(), lanes, s.start(), s.end()));
    }
}
