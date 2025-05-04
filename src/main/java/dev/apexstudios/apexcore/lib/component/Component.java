package dev.apexstudios.apexcore.lib.component;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.ScheduledForRemoval
public interface Component<TBase extends Component<TBase, TObj>, TObj> extends ComponentHolder<TBase, TObj> {

}
