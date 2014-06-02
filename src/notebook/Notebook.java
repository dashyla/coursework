
package notebook;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import javax.swing.table.*;
import java.util.regex.*;

public class Notebook 
{

   
    JFrame f;
    String fileName;
    EditDialog edit;
    
    //Коллекция записей
    ArrayList<Record> all;
    
    DefaultTableModel mainTm;
    JTable mainTable;
    ListSelectionModel selectModel;
    int ind=-1;
    
    public Notebook()
    {
        f=new JFrame("Записная книга");
        f.setSize(500,300);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // создаем список записей
        all = new ArrayList<Record>();
 
        //Создаем модель таблице, в которой будет четыре столбца
        mainTm = new DefaultTableModel();
        mainTm.addColumn("Имя");
        mainTm.addColumn("Дата рождения");
        mainTm.addColumn("E-mail");
        mainTm.addColumn("Группа");
            
        mainTable = new JTable(mainTm)
        {
            public boolean isCellEditable(int rowIndex, int colIndex) 
            {
                    return false; //Запрет на редактирование таблицы
            }
        };
                
        
        // Помещаем таблицу в прокручиваемое поле
        JScrollPane scrol = new JScrollPane(mainTable);
        // В таблице можно будет выделить только одну запись
        mainTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // добавляем к таблице возможность сортировки
        mainTable.setAutoCreateRowSorter(true);
        selectModel = mainTable.getSelectionModel(); 
        // добавляем прокручиваемую таблицу на фрейм
        f.add(scrol, BorderLayout.CENTER);
        
        JMenuBar menuBar = new JMenuBar();
        f.setJMenuBar(menuBar);
        
        JMenu fileMenu = new JMenu("Файл");
        menuBar.add(fileMenu);
    Action createAction = new AbstractAction("Создать")
    {
       public void actionPerformed(ActionEvent event)
       {
           //Очищаем таблицу
          for(int i=0; i<all.size();i++)
          {
            mainTm.removeRow(i);
          }
          //Очищаем список
          all.clear();
          fileName=null;
          JOptionPane.showMessageDialog(f, "Новая зпись создана");
       }
        };
        JMenuItem createMenu = new JMenuItem(createAction);
        fileMenu.add(createMenu);
       
    // Загрузка данных из внешнего файла
    Action loadAction = new AbstractAction("Загрузить")
    {
       public void actionPerformed(ActionEvent event)
       {
           //Выбираем файл
          JFileChooser jf= new JFileChooser();
          int result = jf.showOpenDialog(null);
          if(result==JFileChooser.APPROVE_OPTION)
          {
              try 
              {
                   fileName = jf.getSelectedFile().getAbsolutePath();
                   //Очищаем таблицу
                    for(int i=0; i<all.size();i++)
                    {
                        mainTm.removeRow(i);
                    }
                    //Очищаем список
                    all.clear();
                   //Десериализация файла
                    Load(fileName);
                      for(Record r : all)
                      {
                            // Заполняем таблицу
                            mainTm.addRow(new String[] {r.getName(),r.getBirthday(),r.getEmail(),r.getGroup()});
                      }
                } 
                catch (FileNotFoundException ex) 
                {
                    JOptionPane.showMessageDialog(f, "Такого файла не существует");
                } 
                catch (IOException ex) 
                {
                    JOptionPane.showMessageDialog(f, "Исключение ввода-вывода");
                } 
                catch (ClassNotFoundException ex) 
                {
                    JOptionPane.showMessageDialog(f, "Класс программы не существует");
                } 
          }
        }
       };
        JMenuItem loadMenu = new JMenuItem(loadAction);
        fileMenu.add(loadMenu);
        
     // Пункт для сохранения файла
     Action saveAction = new AbstractAction("Сохранить")
     {
       public void actionPerformed(ActionEvent event)
       {
          try
           {
               if(fileName==null)
               {
                   //создаем диалоговое окно выбора файла
                    JFileChooser jf= new JFileChooser();
                    int result = jf.showSaveDialog(null);
                    if(result==JFileChooser.APPROVE_OPTION)
                    {
                        fileName = jf.getSelectedFile().getName();
                    }
               }
               // Сериализуем коллекцию в файл
               Save(fileName);
           }
           catch(IOException ex)
           {
              JOptionPane.showMessageDialog(f, "Ошибка ввода-вывода");
           }
       }
        };
        JMenuItem saveMenu = new JMenuItem(saveAction);
        fileMenu.add(saveMenu);
        
        // Сохранение как
    Action saveasAction = new AbstractAction("Сохранить как")
    {
       public void actionPerformed(ActionEvent event)
       {
           try
           {
               JFileChooser jf= new JFileChooser();
               int result = jf.showSaveDialog(null);
               if(result==JFileChooser.APPROVE_OPTION)
               {
                   fileName = jf.getSelectedFile().getAbsolutePath();
                   Save(fileName);
               }
           }
           catch(IOException ex)
           {
              JOptionPane.showMessageDialog(f, "Ошибка ввода-вывода");
           }       
       }
        };
        JMenuItem saveasMenu = new JMenuItem(saveasAction);
        fileMenu.add(saveasMenu);
    // пункт меню выйти
    Action exitAction = new AbstractAction("Выйти")
    {
       public void actionPerformed(ActionEvent event)
       {
           //получаем выбранное пользователем занчение по поводу сохранения
            int response = JOptionPane.showConfirmDialog(f,"Сохранить текущую запись?", 
                    "Сохранить текущую запись?",JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            // если пользователь выбрал сохранить - сохраняем в файле список записей
            if(response==JOptionPane.YES_OPTION)
            {
                try
               {
                   JFileChooser jf= new JFileChooser();
                   int result = jf.showSaveDialog(null);
                   if(result==JFileChooser.APPROVE_OPTION)
                   {
                       fileName = jf.getSelectedFile().getName();
                       Save(fileName);
                   }
               }
               catch(IOException ex)
               {
                  JOptionPane.showMessageDialog(f, "Ошибка ввода-вывода");
               }
           }
            // окончательный выход из программы
            System.exit(0);
           
       }
        };
        JMenuItem exitMenu = new JMenuItem(exitAction);
        fileMenu.add(exitMenu);
        
        /*Меню Правка*/
        JMenu editMenu = new JMenu("Правка");
        menuBar.add(editMenu);
        
        Action addAction = new AbstractAction("Добавить")
        {
           public void actionPerformed(ActionEvent event)
           {
               // Вызываем диалоговое окно для создания новой записи
               
                edit = new EditDialog(f, "Создать запись", null);
                edit.setVisible(true);
           }
            };
        JMenuItem addMenu = new JMenuItem(addAction);
        editMenu.add(addMenu);
        
        Action changeAction = new AbstractAction("Редактировать")
        {
           public void actionPerformed(ActionEvent event)
           {
               // Поулчаем индекс выделенной строки
               ind = mainTable.getSelectedRow();
               // если имеется выделенная строка
               if(ind>-1)
               {
                   // получаем выделенную запись
                   Record tempRecord = new Record ();
                    tempRecord.setName(mainTable.getValueAt(ind,0).toString());
                    tempRecord.setBirthday(mainTable.getValueAt(ind,1).toString());
                    tempRecord.setEmail(mainTable.getValueAt(ind,2).toString());
                    tempRecord.setGroup(mainTable.getValueAt(ind,3).toString());
                   // Вызываем диалоговое окно для редактирования
                   // Передавая ему индекс выделенной строки
                    edit = new EditDialog(f, "Редактировать запись",tempRecord);
                    edit.setVisible(true);
                }       
           }
        };
        JMenuItem changeMenu = new JMenuItem(changeAction);
        editMenu.add(changeMenu);
		
       // Поиск нужной записи
        Action findAction = new AbstractAction("Найти")
        {
           public void actionPerformed(ActionEvent event)
           {
               // Вызываем диалоговое окно для поиска
		FindDialog edit = new FindDialog(f, "Найти запись");
                edit.setVisible(true);
           }
        };
        JMenuItem findMenu = new JMenuItem(findAction);
        editMenu.add(findMenu);
        
        // Удаление записи
        Action removeAction = new AbstractAction("Удалить")
        {
           public void actionPerformed(ActionEvent event)
           {
               // Получаем индекс выделенной строки 
               ind = mainTable.getSelectedRow();
               if(ind>-1)
               {
                int counter=-1;
                Record oldRecord = new Record ();
                oldRecord.setName(mainTable.getValueAt(ind,0).toString());
                oldRecord.setBirthday(mainTable.getValueAt(ind,1).toString());
                oldRecord.setEmail(mainTable.getValueAt(ind,2).toString());
                oldRecord.setGroup(mainTable.getValueAt(ind,3).toString());
                // Ищем индекс записи в массиве
                    for(Record r : all)
                    {
                        counter++;
                        if(oldRecord.equals(r))
                        {
                            break;
                        }
                    }
                    // И удаляем ее из коллекции all и из таблицы
                    all.remove(counter);
                    mainTm.removeRow(counter);    
                } 
           }
            };
        JMenuItem removeMenu = new JMenuItem(removeAction);
        editMenu.add(removeMenu);
        
        // Создаем панель инструментов, которая дублирует некоторые функции меню
          JToolBar toolbar = new JToolBar("Toolbar", JToolBar.HORIZONTAL);
          // кнопка создания новой записной книги
          JButton newbutton = new JButton(new ImageIcon("document.png"));
          newbutton.addActionListener(new ActionListener()
            {
              public void actionPerformed(ActionEvent event)
              { 
                //Очищаем таблицу
                for(int i=0; i<all.size();i++)
                {
                    mainTm.removeRow(i);
                }
                //Очищаем список
                all.clear();
                fileName=null;
                JOptionPane.showMessageDialog(f, "Новая записная книга создана");
              }
            });
          toolbar.add(newbutton);
          // кнопка сохранения записной книги
          JButton savebutton = new JButton(new ImageIcon("save.png"));
          savebutton.addActionListener(new ActionListener()
            {
              public void actionPerformed(ActionEvent event)
              { 
               try
                {
                    if(fileName==null)
                    {
                     JFileChooser jf= new JFileChooser();
                     int result = jf.showSaveDialog(null);
                     if(result==JFileChooser.APPROVE_OPTION)
                    {
                        fileName = jf.getSelectedFile().getName();
                    }
                    }
                    Save(fileName);
                }
                catch(IOException ex)
                {
                   JOptionPane.showMessageDialog(f, "Ошибка ввода-вывода");
                }
              }
            });
          toolbar.add(savebutton);
          
          // Кнопка Сохранить как на панели инструментов
          JButton saveasbutton = new JButton(new ImageIcon("save_as.png"));
          saveasbutton.addActionListener(new ActionListener()
            {
              public void actionPerformed(ActionEvent event)
              { 
                try
                {
                    JFileChooser jf= new JFileChooser();
                    int result = jf.showSaveDialog(null);
                    if(result==JFileChooser.APPROVE_OPTION)
                    {
                        fileName = jf.getSelectedFile().getAbsolutePath();
                        Save(fileName);
                    }
                }
                catch(IOException ex)
                {
                   JOptionPane.showMessageDialog(f, "Ошибка ввода-вывода");
                }
              }
            });
          toolbar.add(saveasbutton);
          // кнопка выхода
          JButton exitbutton = new JButton(new ImageIcon("exit.png"));
          exitbutton.addActionListener(new ActionListener()
            {
              public void actionPerformed(ActionEvent event)
              { 
                int response = JOptionPane.showConfirmDialog(f,"Сохранить текущую запись?", 
                    "Сохранить текущую запись?",JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if(response==JOptionPane.YES_OPTION)
            {
                try
               {
                   JFileChooser jf= new JFileChooser();
                   int result = jf.showSaveDialog(null);
                   if(result==JFileChooser.APPROVE_OPTION)
                   {
                       fileName = jf.getSelectedFile().getName();
                       Save(fileName);
                   }
               }
               catch(IOException ex)
               {
                  JOptionPane.showMessageDialog(f, "Ошибка ввода-вывода");
               }
           }
            System.exit(0);
              }
            });
          toolbar.add(exitbutton);
          //запрещаем перемещение панели тулбара
          toolbar.setFloatable(false);
          // добавляем тулбар на фрейм
          f.add(toolbar,BorderLayout.NORTH);
        // делаем фрейм видимым
        f.setVisible(true);
    }
    
    // Десериализация из файла
    public void Load(String filename) throws IOException, FileNotFoundException, ClassNotFoundException
    {
            // Получаем файловый поток
            FileInputStream fis = new FileInputStream(filename);
            // Созадем по нему поток объектов
            ObjectInputStream ois = new ObjectInputStream(fis);
            Record pe;
            try
            {
                all.clear();
                // Считываем из файла все записи
                while ((pe =(Record)ois.readObject())!=null)
                { 
                    if (pe != null)
                    {
                        all.add(pe);
                    }
                }
            }
            catch(EOFException e)
            {}
            finally
            {
                // Закрытие потоков
                ois.close();
                fis.close();
            }        
    }
    
    // Сериализация в файл
        public void Save(String filename) throws IOException, FileNotFoundException
        {
            // Создаем файловый поток для записи
            FileOutputStream fos = new FileOutputStream(filename);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            
            // Записываем по одной записи в файл
            for(Record pe : all)
            {
                oos.writeObject(pe);
            }
            // Закрываем поток
            oos.flush();
            oos.close();
            fos.close();
        }
    
    public static void main(String[] args) {
        // TODO code application logic here
        SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        new Notebook();
      }
    });
    }
  
    // диалоговое окно добавления новй записи/редактирования старой записи
class EditDialog extends JDialog
{
  public EditDialog(JFrame owner, String title, final Record oldRecord)
  {
    super(owner, title, true);
    /*
     * Создаем элементы управления
     */
    JLabel lab1=new JLabel("Имя :");
        lab1.setBounds(10, 10, 50, 20);
        lab1.setFont(new Font("Arial",0,11));
        
     JLabel lab2=new JLabel("Дата рождения :");
        lab2.setBounds(10, 40, 80, 20);
        lab2.setFont(new Font("Arial",0,11));
        
     JLabel lab3=new JLabel("E-mail :");
        lab3.setBounds(10, 70, 50, 20);
        lab3.setFont(new Font("Arial",0,11));
    
     JLabel lab4=new JLabel("Группа :");
        lab4.setBounds(10, 100, 50, 20);
        lab4.setFont(new Font("Arial",0,11));
        
     final JTextField nameField = new JTextField();
     nameField.setBounds(100, 10, 200, 20);
     
     final JTextField birthField = new JTextField();
     birthField.setBounds(100, 40, 200, 20);
     
     final JTextField emailField = new JTextField();
     emailField.setBounds(100, 70, 200, 20);
     
     final JTextField groupField = new JTextField();
     groupField.setBounds(100, 100, 200, 20);
     
    JButton ok = new JButton("OK");
    ok.setBounds(10, 150, 90, 20);
    ok.setFont(new Font("Arial",0,11));
    ok.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent event)
      {
          int counter=-1;// счетчик записей
          
         try
         {
          if(testEmail(emailField.getText())==false)
          {
              throw new Exception("Некорректно введен e-mail");
          }
          if(testName(nameField.getText())==false)
          {
              throw new Exception("Некорректно введено имя");
          }
          if(testName(nameField.getText())==false)
          {
              throw new Exception("Некорректно введена фамилия");
          }
          
          // Создаем временную запись
          Record tempRec = new Record();
          tempRec.setName(nameField.getText());
          tempRec.setBirthday(birthField.getText());
          tempRec.setEmail(emailField.getText());
          tempRec.setGroup(groupField.getText());
         // Если такая запись еще не существует
         // то добавляем эту запись
         if(compare(tempRec)==false)
         {
             //Если индекс текущей записи не равен -1
             // То мы редактируем текущую запись - 
             // удаляем ее, а вместо нее вставляем новую
             // иначе мы просто создаем новую запись
             // Если запись передана в объект диалогового окна
             if(oldRecord!=null)
             {
                 // Ищем индекс записи в массиве
                 for(Record r : all)
                 {
                     counter++;
                     if(oldRecord.equals(r))
                     {
                         break;
                     }
                 }
                 mainTm.removeRow(counter);
                 all.remove(counter);
             }
             // Добавляем запись в список и в таблицу
             mainTm.addRow(new String[]{tempRec.getName(),tempRec.getBirthday(), 
                 tempRec.getEmail(), tempRec.getGroup()});
             
             all.add(tempRec);     
         }
             ind=-1;
             setVisible(false);
      }
         catch(Exception e)
         {
             // И извещаем пользователя, что надо ввести число
             JOptionPane.showMessageDialog(null, e.getMessage());
         }
      }
    });
    
    JButton delet = new JButton("Отмена");
    delet.setBounds(150, 150, 90, 20);
    delet.setFont(new Font("Arial",0,11));
    delet.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent event)
      { 
        setVisible(false); 
        ind=-1;
      }
    });
    // Если число не равно -1, то значить мы редактируем запись
    // Поэтому вставляем данные этой записи в соответствующие поля
    if(oldRecord!=null)
    {
        nameField.setText(oldRecord.getName());
        birthField.setText(oldRecord.getBirthday());
        emailField.setText(oldRecord.getEmail());
        groupField.setText(oldRecord.getGroup());
    }
    
    JPanel panel = new JPanel();
    panel.setLayout(null);
    panel.add(ok);
    panel.add(delet);
    panel.add(nameField);
    panel.add(birthField);
    panel.add(emailField);
    panel.add(groupField);
    panel.add(lab1);
    panel.add(lab2);
    panel.add(lab3);
    panel.add(lab4);
    //доабвляем панель на диалоговое окно
    add(panel, BorderLayout.CENTER);
    setSize(350, 220);
  }
  // метод сравнения записей
  public boolean compare(Record r)
  {
      // по всем записям смотрим, есть ли уже такая запись
      for(Record rr : all)
      {
          if(rr.equals(r))
          {
              return true;
          }
      }
      return false;
  }
  
  public boolean testEmail(String email){
      // регулярное выражения для валидации
    Pattern p = Pattern.compile("(([a-zA-Z][\\w]*)@[\\w[.]]*\\.+([a-z]+))");
    // сопоставляем его со строкой
    Matcher m = p.matcher(email);
    // получаем результат сопоставления - корректно или нет
    boolean b = m.matches();
    return b;
    }
  public boolean testName(String name){
    Pattern p = Pattern.compile("(([a-zA-Z]|[а-яА-Я])*)");
    Matcher m = p.matcher(name);
    boolean b = m.matches();
    return b;
    }
}
// диалоговое окно для поиска файлов
class FindDialog extends JDialog
{
    int mode =0;
	//Выпадающий список для параметров поиска
    JComboBox modes;
    // модель таблицы
    DefaultTableModel tm;
    // сама таблица
    JTable phoneTable;
    
    // конструктор диалогового окна
  public FindDialog(JFrame owner, String title)
  {
    super(owner, title, true);
    // установка элементов управления в окне
    JLabel lab1=new JLabel("Ключ поиска :");
        lab1.setBounds(10, 10, 100, 20);
        lab1.setFont(new Font("Arial",0,11));
        
     JLabel lab2=new JLabel("Параметр поиска");
        lab2.setBounds(300, 10, 110, 20);
        lab2.setFont(new Font("Arial",0,11));
        
     final JTextField fio = new JTextField();
     fio.setBounds(120, 10, 150, 20);
     
     // создаем параметры поиска
     String options[] = {"По имени", "По дате рождения", "По email", "По группе"};
     
     // добавляем эти параметры в выпадающий список
        modes = new JComboBox (options);
        // прослушиваем событие выбора элемента из списка
	modes.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                  mode = modes.getSelectedIndex();
            }
      });
        // установка его границ
        modes.setBounds(420, 10, 100, 20);
    
        // код кнопки Найти
    JButton apply = new JButton("Найти");
    apply.setBounds(220, 40, 90, 20);
    apply.setFont(new Font("Arial",0,11));
    // код нажатия на кнопку
    apply.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent event)
      {
          // по нажатию получаем ключевое слово для поиска
        String string4find = fio.getText();
        // и если оно имеется, получаем выбранный параметр поиска
          if(!string4find.equals(""))
          {
             if(mode==0) 
             {
                 // пробегаемся по всем записям
                for(Record r : all)
		{		
                    // и если по выбранному параметру записи совпадают
                    if(r.getName().equals(string4find))
                    {
                        // добавляем совпавшие записи в таблицу
                        tm.addRow(new String[] {r.getName(),r.getBirthday(),r.getEmail(),r.getGroup()});
                    }
		}
             }
             else if(mode==1)
             {
                 for(Record r : all)
		{		
                    if(r.getBirthday().equals(string4find))
                    {
                        tm.addRow(new String[] {r.getName(),r.getBirthday(),r.getEmail(),r.getGroup()});
                    }
		}
             }
             else if(mode==2)
             {
                 for(Record r : all)
		{		
                    if(r.getEmail().equals(string4find))
                    {
                        tm.addRow(new String[] {r.getName(),r.getBirthday(),r.getEmail(),r.getGroup()});
                    }
		}
             }
             else if(mode==3)
             {
                 for(Record r : all)
		{		
                    if(r.getGroup().equals(string4find))
                    {
                        tm.addRow(new String[] {r.getName(),r.getBirthday(),r.getEmail(),r.getGroup()});
                    }
		}
             }
          }
      }
    });
            // установка модели таблицы
            tm = new DefaultTableModel();
            tm.addColumn("Имя");
            tm.addColumn("Дата рождения");
            tm.addColumn("E-mail");
            tm.addColumn("Группа");
            
            // запрещаем редактирование строк таблицы
            phoneTable = new JTable(tm)
            {
                public boolean isCellEditable(int rowIndex, int colIndex) 
                {
                     return false; //Запрет на редактирование таблицы
                }
            };
            // создаем для таблицы прокручиваемую панель
            JScrollPane scrol = new JScrollPane(phoneTable);
            scrol.setSize(520,120);
            scrol.setLocation(10, 80);
            
            phoneTable.setPreferredScrollableViewportSize(new Dimension(520,120));
        
            // доабвляем все элементы на новую панель
            JPanel panel = new JPanel();
            panel.setLayout(null);
            panel.add(apply);
            panel.add(fio);
            panel.add(lab1);
            panel.add(lab2);
            panel.add(scrol);
            panel.add(modes);
            // а панель добавляем в диалоговое окно
            add(panel, BorderLayout.CENTER);
            setSize(550, 220);
  }
  
}


}
