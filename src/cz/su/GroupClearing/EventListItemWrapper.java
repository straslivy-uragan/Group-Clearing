package cz.su.GroupClearing;

import android.view.View;
import android.widget.TextView;

public class EventListItemWrapper {
   View base = null;
   TextView title = null;
   TextView subtitle = null;

   EventListItemWrapper(View aBase) {
      base = aBase;
   }

   TextView getTitle() {
      if (title == null)
      {
         title = (TextView)base.findViewById(R.id.eventListItemTitle);
      }
      return title;
   }

   TextView getSubtitle() {
      if (subtitle == null)
      {
         subtitle = (TextView)base.findViewById(R.id.eventListItemSubtitle);
      }
      return subtitle;
   }
}
